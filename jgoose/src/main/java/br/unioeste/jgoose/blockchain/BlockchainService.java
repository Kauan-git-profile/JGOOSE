package br.unioeste.jgoose.blockchain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.utils.Numeric;

/**
 * Camada on-chain do TCC: grava e consulta o hash SHA-256 de cada execução no contrato
 * RastreabilidadeRegistry (blockchain/contracts/RastreabilidadeRegistry.sol).
 *
 * Funções do contrato usadas:
 *   registrarHash(uint256 execucaoId, bytes32 hashArtefato)
 *   quantidadeRegistros(uint256 execucaoId) -> uint256
 *   obterRegistro(uint256 execucaoId, uint256 indice) -> (bytes32, uint256, address)
 *
 * Configuração: variáveis de ambiente OU arquivo jgoose-blockchain.properties
 * (no diretório de execução, ou o caminho em JGOOSE_BLOCKCHAIN_CONFIG).
 * A variável de ambiente tem prioridade. Chaves:
 *   JGOOSE_RPC_URL           (padrão: http://127.0.0.1:8545)
 *   JGOOSE_CHAIN_ID          (padrão: 31337, a rede local do Hardhat)
 *   JGOOSE_PRIVATE_KEY       (obrigatória; chave de uma conta de TESTE)
 *   JGOOSE_CONTRACT_ADDRESS  (obrigatória; endereço impresso no deploy)
 */
public class BlockchainService {

    /** Uma versão do hash registrada on-chain para uma execução. */
    public static class RegistroOnChain {
        /** Número da versão (1 = primeiro registro). */
        public final int versao;
        /** SHA-256 em hexadecimal minúsculo (64 caracteres). */
        public final String hash;
        public final BigInteger timestamp;
        /** Endereço da conta que gravou. */
        public final String responsavel;

        RegistroOnChain(int versao, String hash, BigInteger timestamp, String responsavel) {
            this.versao = versao;
            this.hash = hash;
            this.timestamp = timestamp;
            this.responsavel = responsavel;
        }
    }

    private static final String ARQUIVO_CONFIG_PADRAO = "jgoose-blockchain.properties";
    private static final Pattern ENDERECO = Pattern.compile("^0x[0-9a-fA-F]{40}$");
    private static final Pattern CHAVE_PRIVADA = Pattern.compile("^(0x)?[0-9a-fA-F]{64}$");
    private static final Pattern HASH_SHA256 = Pattern.compile("^[0-9a-fA-F]{64}$");
    /** SHA-256 da string vazia; usado só no teste rápido (main). */
    private static final String SHA256_DE_TESTE = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    /** Serializa os envios de transações desta conta: sem isso, duas threads leem o mesmo nonce. */
    private static final Object LOCK_ENVIO = new Object();

    private final Web3j web3j;
    private final Credentials credentials;
    private final String contractAddress;
    private final long chainId;

    /** True se a chave privada e o endereço do contrato estão configurados. */
    public static boolean configurado() {
        Properties arq = carregarArquivo();
        return valor("JGOOSE_PRIVATE_KEY", null, arq) != null
                && valor("JGOOSE_CONTRACT_ADDRESS", null, arq) != null;
    }

    public BlockchainService() {
        Properties arq = carregarArquivo();
        String rpcUrl = valor("JGOOSE_RPC_URL", "http://127.0.0.1:8545", arq);
        String chain = valor("JGOOSE_CHAIN_ID", "31337", arq);
        String chave = exigir("JGOOSE_PRIVATE_KEY", arq);
        String endereco = exigir("JGOOSE_CONTRACT_ADDRESS", arq);

        if (!CHAVE_PRIVADA.matcher(chave).matches()) {
            throw new IllegalStateException("JGOOSE_PRIVATE_KEY inválida (esperado 64 caracteres hexadecimais).");
        }
        if (!ENDERECO.matcher(endereco).matches()) {
            throw new IllegalStateException("JGOOSE_CONTRACT_ADDRESS inválido (esperado 0x + 40 hexadecimais).");
        }
        try {
            this.chainId = Long.parseLong(chain);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("JGOOSE_CHAIN_ID inválido: " + chain);
        }
        this.credentials = Credentials.create(chave);
        this.contractAddress = endereco;
        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }

    /**
     * Registra uma NOVA versão do hash para a execução e espera a confirmação.
     * @param hashSha256Hex SHA-256 em hexadecimal (64 caracteres), o mesmo guardado em hash_sha256
     * @return o hash da transação (txHash)
     */
    public String registrarHash(long execucaoId, String hashSha256Hex) {
        synchronized (LOCK_ENVIO) {
        try {
            verificarRede();

            Function fn = new Function(
                    "registrarHash",
                    Arrays.<Type>asList(new Uint256(BigInteger.valueOf(execucaoId)), paraBytes32(hashSha256Hex)),
                    Collections.<TypeReference<?>>emptyList());
            String data = FunctionEncoder.encode(fn);
            String from = credentials.getAddress();

            // A estimativa de gas também revela, antes de enviar, se o contrato vai recusar o registro.
            EthEstimateGas est = web3j.ethEstimateGas(
                    Transaction.createEthCallTransaction(from, contractAddress, data)).send();
            if (est.hasError()) {
                throw new IllegalStateException("O contrato recusou o registro: " + est.getError().getMessage());
            }
            BigInteger gasLimit = est.getAmountUsed().multiply(BigInteger.valueOf(12)).divide(BigInteger.TEN); // +20%
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();

            RawTransactionManager tm = new RawTransactionManager(web3j, credentials, chainId);
            EthSendTransaction resp = tm.sendTransaction(gasPrice, gasLimit, contractAddress, data, BigInteger.ZERO);
            if (resp.hasError()) {
                throw new IllegalStateException("Erro ao enviar a transação: " + resp.getError().getMessage());
            }

            String txHash = resp.getTransactionHash();
            TransactionReceipt receipt =
                    new PollingTransactionReceiptProcessor(web3j, 1000, 30).waitForTransactionReceipt(txHash);
            if (!receipt.isStatusOK()) {
                throw new IllegalStateException("Transação revertida pela rede: " + txHash);
            }
            return txHash;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao registrar hash on-chain: " + e.getMessage(), e);
        }
        }
    }

    /** Quantas versões de hash existem on-chain para a execução (0 = nunca registrada). */
    public long quantidadeRegistros(long execucaoId) {
        Function fn = new Function(
                "quantidadeRegistros",
                Arrays.<Type>asList(new Uint256(BigInteger.valueOf(execucaoId))),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> out = chamar(fn);
        return ((BigInteger) out.get(0).getValue()).longValue();
    }

    /** Lê uma versão específica (indice começa em 0; versão = indice + 1). */
    public RegistroOnChain obterRegistro(long execucaoId, long indice) {
        Function fn = new Function(
                "obterRegistro",
                Arrays.<Type>asList(new Uint256(BigInteger.valueOf(execucaoId)),
                                    new Uint256(BigInteger.valueOf(indice))),
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Bytes32>() {},
                        new TypeReference<Uint256>() {},
                        new TypeReference<Address>() {}));
        List<Type> out = chamar(fn);
        return new RegistroOnChain(
                (int) indice + 1,
                Numeric.toHexStringNoPrefix((byte[]) out.get(0).getValue()),
                (BigInteger) out.get(1).getValue(),
                (String) out.get(2).getValue());
    }

    /** Todas as versões registradas para a execução, da mais antiga para a mais recente. */
    public List<RegistroOnChain> obterTodos(long execucaoId) {
        long n = quantidadeRegistros(execucaoId);
        List<RegistroOnChain> lista = new ArrayList<RegistroOnChain>();
        for (long i = 0; i < n; i++) {
            lista.add(obterRegistro(execucaoId, i));
        }
        return lista;
    }

    public void encerrar() {
        web3j.shutdown();
    }

    // ---------------------------------------------------------------- internos

    private List<Type> chamar(Function fn) {
        try {
            EthCall call = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                            credentials.getAddress(), contractAddress, FunctionEncoder.encode(fn)),
                    DefaultBlockParameterName.LATEST).send();
            if (call.hasError()) {
                throw new IllegalStateException("Erro na consulta on-chain: " + call.getError().getMessage());
            }
            String retorno = call.getValue();
            if (retorno == null || "0x".equals(retorno)) {
                throw new IllegalStateException("Nenhum contrato encontrado em " + contractAddress
                        + ". Se o nó local foi reiniciado, refaça o deploy e confira JGOOSE_CONTRACT_ADDRESS.");
            }
            return FunctionReturnDecoder.decode(retorno, fn.getOutputParameters());
        } catch (IOException e) {
            throw new IllegalStateException("Falha de comunicação com a rede (o nó está rodando?): " + e.getMessage(), e);
        }
    }

    private static Bytes32 paraBytes32(String hexSha256) {
        if (hexSha256 == null || !HASH_SHA256.matcher(hexSha256).matches()) {
            throw new IllegalArgumentException("Hash SHA-256 inválido (esperado 64 caracteres hexadecimais): " + hexSha256);
        }
        return new Bytes32(Numeric.hexStringToByteArray(hexSha256));
    }

    /**
     * Confere a rede (Chain ID) e se existe mesmo um contrato no endereço configurado.
     * Sem a segunda checagem, um endereço errado (ex.: nó local reiniciado, contrato perdido)
     * faria o envio "dar certo" sem registrar nada.
     */
    private void verificarRede() throws IOException {
        EthChainId r = web3j.ethChainId().send();
        if (r.hasError()) {
            throw new IllegalStateException("Não foi possível consultar a rede: " + r.getError().getMessage());
        }
        long real = r.getChainId().longValue();
        if (real != chainId) {
            throw new IllegalStateException("Chain ID da rede é " + real
                    + ", mas JGOOSE_CHAIN_ID está como " + chainId + ".");
        }
        EthGetCode code = web3j.ethGetCode(contractAddress, DefaultBlockParameterName.LATEST).send();
        if (code.hasError() || code.getCode() == null || "0x".equals(code.getCode())) {
            throw new IllegalStateException("Nenhum contrato encontrado em " + contractAddress
                    + ". Se o nó local foi reiniciado, refaça o deploy e confira JGOOSE_CONTRACT_ADDRESS.");
        }
    }

    private static Properties carregarArquivo() {
        Properties p = new Properties();
        String caminho = System.getenv("JGOOSE_BLOCKCHAIN_CONFIG");
        File f = new File((caminho == null || caminho.trim().isEmpty()) ? ARQUIVO_CONFIG_PADRAO : caminho.trim());
        if (f.isFile()) {
            try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
                p.load(r);
            } catch (IOException e) {
                System.err.println("[Blockchain] Não foi possível ler " + f.getAbsolutePath() + ": " + e.getMessage());
            }
        }
        return p;
    }

    /** Variável de ambiente > arquivo de propriedades > padrão. Vazio conta como ausente. */
    private static String valor(String nome, String padrao, Properties arq) {
        String v = System.getenv(nome);
        if (v == null || v.trim().isEmpty()) {
            v = arq.getProperty(nome);
        }
        if (v == null || v.trim().isEmpty()) {
            return padrao;
        }
        return v.trim();
    }

    private static String exigir(String nome, Properties arq) {
        String v = valor(nome, null, arq);
        if (v == null) {
            throw new IllegalStateException("Configuração obrigatória não definida: " + nome);
        }
        return v;
    }

    /** Teste rápido sem a interface: registra um hash fictício num id alto e lê de volta. */
    public static void main(String[] args) {
        BlockchainService s = new BlockchainService();
        long id = 900000000L + (System.currentTimeMillis() % 1000000L);
        String hash = SHA256_DE_TESTE;
        String tx = s.registrarHash(id, hash);
        System.out.println("txHash: " + tx);
        for (RegistroOnChain r : s.obterTodos(id)) {
            System.out.println("versão " + r.versao + ": " + r.hash + " @ " + r.timestamp + " por " + r.responsavel);
        }
        s.encerrar();
    }
}
