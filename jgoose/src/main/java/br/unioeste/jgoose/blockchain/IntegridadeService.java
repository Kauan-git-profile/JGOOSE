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
            String tx = chain.registrarHash(execucaoId, hash);
            dao.salvarTxHash(execucaoId, tx);
            return "[Integridade] Execução " + execucaoId + ": hash " + hash + " registrado on-chain. txHash: " + tx;
        } catch (Exception e) {
            return "[Integridade] Execução " + execucaoId + ": hash " + hash
                    + " salvo no banco, mas o envio on-chain FALHOU: " + e.getMessage();
        } finally {
            if (chain != null) {
                chain.encerrar();
            }
        }
    }
}
