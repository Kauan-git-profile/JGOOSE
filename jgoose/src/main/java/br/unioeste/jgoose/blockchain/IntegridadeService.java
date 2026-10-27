package br.unioeste.jgoose.blockchain;

import br.unioeste.jgoose.persistence.HashService;
import br.unioeste.jgoose.persistence.RastreabilidadeDAO;

/**
 * Fluxo "salvou no banco -> calcula hash -> registra on-chain".
 * Nunca lança exceção para quem chama: falhas viram uma mensagem, para que um problema
 * na blockchain não derrube a geração da matriz (baixo impacto sobre a ferramenta CASE).
 */
public class IntegridadeService {

    /**
     * Calcula o hash SHA-256 da execução, salva em execucao_mapeamento.hash_sha256 e, se a
     * blockchain estiver configurada, registra o hash no contrato (salvando o txHash).
     * O identificador que liga banco e blockchain é o próprio id da execução.
     * @return mensagem de status para o console
     */
    public static String registrarExecucao(long execucaoId) {
        String hash;
        RastreabilidadeDAO dao = new RastreabilidadeDAO();
        try {
            hash = new HashService().calcularHashExecucao(execucaoId);
            dao.salvarHash(execucaoId, hash);
        } catch (Exception e) {
            e.printStackTrace();
            return "[Integridade] Falha ao calcular/salvar o hash da execução " + execucaoId + ": " + e.getMessage();
        }

        if (!BlockchainService.configurado()) {
            return "[Integridade] Execução " + execucaoId + ": hash " + hash
                    + " salvo no banco. Blockchain NÃO configurada (defina JGOOSE_PRIVATE_KEY e JGOOSE_CONTRACT_ADDRESS)"
                    + " - hash não foi enviado on-chain.";
        }

        BlockchainService chain = null;
        try {
            chain = new BlockchainService();

            // Se a versão mais recente registrada já é igual ao hash atual, não há nada novo a registrar
            // (evita versões repetidas quando o diagrama é gerado de novo sem ter sido alterado).
            long versoes = chain.quantidadeRegistros(execucaoId);
            if (versoes > 0 && chain.obterRegistro(execucaoId, versoes - 1).hash.equalsIgnoreCase(hash)) {
                return "[Integridade] Execução " + execucaoId + ": sem alterações em relação à versão " + versoes
                        + " já registrada na blockchain; nada a registrar.";
            }

            String tx = chain.registrarHash(execucaoId, hash);
            dao.salvarTxHash(execucaoId, tx);
            return "[Integridade] Execução " + execucaoId + ": hash " + hash + " registrado on-chain"
                    + (versoes > 0 ? " (nova versão " + (versoes + 1) + ")" : "") + ". txHash: " + tx;
        } catch (Exception e) {
            return "[Integridade] Execução " + execucaoId + ": hash " + hash
                    + " salvo no banco, mas o envio on-chain FALHOU: " + e.getMessage();
        } finally {
            if (chain != null) {
                chain.encerrar();
            }
        }
    }

    /**
     * Dispara {@link #registrarExecucao} em uma thread de fundo, para não travar a interface.
     * Usado tanto pela rastreabilidade horizontal quanto pela vertical.
     */
    public static void registrarEmSegundoPlano(final long execucaoId) {
        if (execucaoId <= 0) {
            return;
        }
        Thread integridade = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(registrarExecucao(execucaoId));
            }
        }, "jgoose-integridade-" + execucaoId);
        integridade.setDaemon(true);
        integridade.start();
    }
}
