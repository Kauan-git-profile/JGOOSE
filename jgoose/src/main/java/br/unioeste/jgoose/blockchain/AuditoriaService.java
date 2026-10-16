package br.unioeste.jgoose.blockchain;

import br.unioeste.jgoose.persistence.DatabaseConnection;
import br.unioeste.jgoose.persistence.HashService;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Módulo de auditoria: recalcula o hash dos dados que estão HOJE no banco e compara
 * com os hashes gravados na blockchain para a mesma execução (mesmo execucaoId).
 *
 * Uso por linha de comando (na pasta onde está o .db, ou com JGOOSE_DB_PATH):
 *   java -cp jgoose-0.3.0-jar-with-dependencies.jar br.unioeste.jgoose.blockchain.AuditoriaService
 *   java -cp ... br.unioeste.jgoose.blockchain.AuditoriaService --id 3
 * Código de saída: 0 = tudo íntegro; 2 = divergência/versão antiga/não registrado; 3 = erro.
 */
public class AuditoriaService {

    public enum Status {
        /** O hash recalculado é igual à versão mais recente registrada on-chain. */
        INTEGRO,
        /** Igual a uma versão antiga: existe um registro mais novo diferente dos dados atuais. */
        VERSAO_ANTERIOR,
        /** Não é igual a nenhuma versão registrada: indício de adulteração do banco. */
        DIVERGENTE,
        /** A execução nunca foi registrada on-chain. */
        NAO_REGISTRADO_ONCHAIN
    }

    public static class Resultado {
        public final long execucaoId;
        public final Status status;
        public final String hashRecalculado;
        public final String hashSalvoNoBanco;
        public final List<BlockchainService.RegistroOnChain> versoes;
        /** Versão (1..n) igual ao hash recalculado, ou 0 se nenhuma. */
        public final int versaoCorrespondente;

        Resultado(long execucaoId, Status status, String hashRecalculado, String hashSalvoNoBanco,
                  List<BlockchainService.RegistroOnChain> versoes, int versaoCorrespondente) {
            this.execucaoId = execucaoId;
            this.status = status;
            this.hashRecalculado = hashRecalculado;
            this.hashSalvoNoBanco = hashSalvoNoBanco;
            this.versoes = versoes;
            this.versaoCorrespondente = versaoCorrespondente;
        }

        /** True se o hash guardado na coluna do banco difere do recalculado (os dados mudaram depois do registro). */
        public boolean colunaHashDesatualizada() {
            return hashSalvoNoBanco != null && !hashSalvoNoBanco.equalsIgnoreCase(hashRecalculado);
        }

        /** True se a execução tem mais de uma versão registrada on-chain. */
        public boolean registrosSuspeitos() {
            return versoes.size() > 1;
        }

        /** True se alguma versão foi gravada por uma conta diferente da que gravou a primeira. */
        public boolean registrantesDiferentes() {
            for (BlockchainService.RegistroOnChain r : versoes) {
                if (!r.responsavel.equalsIgnoreCase(versoes.get(0).responsavel)) {
                    return true;
                }
            }
            return false;
        }

        public String descricao() {
            StringBuilder sb = new StringBuilder();
            sb.append("Execução ").append(execucaoId).append(": ");
            switch (status) {
                case INTEGRO:
                    sb.append("ÍNTEGRO - o hash recalculado do banco é igual ao registrado na blockchain");
                    break;
                case VERSAO_ANTERIOR:
                    sb.append("DESATUALIZADO - os dados do banco correspondem à versão ").append(versaoCorrespondente)
                      .append(" de ").append(versoes.size()).append(", mas há um registro mais recente na blockchain");
                    break;
                case DIVERGENTE:
                    sb.append("ALERTA DE ADULTERAÇÃO - o hash recalculado do banco NÃO corresponde a nenhum registro da blockchain");
                    break;
                default:
                    sb.append("NÃO REGISTRADO na blockchain (nada para comparar)");
                    break;
            }
            sb.append("\n    recalculado (banco agora): ").append(hashRecalculado);
            for (BlockchainService.RegistroOnChain r : versoes) {
                sb.append("\n    on-chain v").append(r.versao).append("             : ").append(r.hash)
                  .append("\n                               registrado em ").append(Instant.ofEpochSecond(r.timestamp.longValue()))
                  .append(" por ").append(r.responsavel);
            }
            if (colunaHashDesatualizada()) {
                sb.append("\n    ATENÇÃO: o hash salvo na coluna hash_sha256 (").append(hashSalvoNoBanco)
                  .append(") difere do recalculado - os dados mudaram depois do registro.");
            }
            if (registrosSuspeitos()) {
                sb.append("\n    AVISO: esta execução tem ").append(versoes.size())
                  .append(" versões registradas on-chain; confirme que todas foram legítimas.");
            }
            if (registrantesDiferentes()) {
                sb.append("\n    AVISO: há registros feitos por contas diferentes (o contrato não restringe quem pode gravar).");
            }
            return sb.toString();
        }
    }

    private final HashService hashService = new HashService();

    public Resultado auditar(BlockchainService chain, long execucaoId) throws SQLException {
        String recalculado = hashService.calcularHashExecucao(execucaoId);
        String salvo = lerHashSalvo(execucaoId);

        List<BlockchainService.RegistroOnChain> versoes = chain.obterTodos(execucaoId);
        if (versoes.isEmpty()) {
            return new Resultado(execucaoId, Status.NAO_REGISTRADO_ONCHAIN, recalculado, salvo, versoes, 0);
        }

        int ultima = versoes.size();
        if (versoes.get(ultima - 1).hash.equalsIgnoreCase(recalculado)) {
            return new Resultado(execucaoId, Status.INTEGRO, recalculado, salvo, versoes, ultima);
        }
        for (int i = ultima - 2; i >= 0; i--) {
            if (versoes.get(i).hash.equalsIgnoreCase(recalculado)) {
                return new Resultado(execucaoId, Status.VERSAO_ANTERIOR, recalculado, salvo, versoes, i + 1);
            }
        }
        return new Resultado(execucaoId, Status.DIVERGENTE, recalculado, salvo, versoes, 0);
    }

    public List<Long> listarExecucoes() throws SQLException {
        List<Long> ids = new ArrayList<Long>();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM execucao_mapeamento ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getLong(1));
            }
        }
        return ids;
    }

    /** Nome do projeto a que a execução pertence (para exibir na tela). */
    public String nomeDoProjeto(long execucaoId) throws SQLException {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT p.nome FROM execucao_mapeamento e JOIN projeto p ON p.id = e.projeto_id WHERE e.id = ?")) {
            ps.setLong(1, execucaoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : "";
            }
        }
    }

    private String lerHashSalvo(long execucaoId) throws SQLException {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT hash_sha256 FROM execucao_mapeamento WHERE id = ?")) {
            ps.setLong(1, execucaoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public static void main(String[] args) {
        Long somenteId = null;
        if (args.length == 2 && "--id".equals(args[0])) {
            somenteId = Long.valueOf(args[1]);
        } else if (args.length != 0) {
            System.err.println("Uso: AuditoriaService [--id <execucaoId>]");
            System.exit(3);
        }

        File db = new File(DatabaseConnection.caminhoDoBanco());
        System.out.println("[Auditoria] Banco: " + db.getAbsolutePath());
        if (!db.isFile()) {
            System.err.println("Arquivo de banco não encontrado. Rode na pasta onde o .db foi criado ou defina JGOOSE_DB_PATH.");
            System.exit(3);
        }
        if (!BlockchainService.configurado()) {
            System.err.println("Blockchain não configurada (JGOOSE_PRIVATE_KEY e JGOOSE_CONTRACT_ADDRESS).");
            System.exit(3);
        }

        BlockchainService chain = null;
        int codigo = 0;
        try {
            DatabaseConnection.criarSchema(); // idempotente; também migra bancos antigos (coluna tx_hash)
            chain = new BlockchainService();
            AuditoriaService svc = new AuditoriaService();
            List<Long> ids = new ArrayList<Long>();
            if (somenteId != null) {
                ids.add(somenteId);
            } else {
                ids = svc.listarExecucoes();
            }
            if (ids.isEmpty()) {
                System.out.println("Nenhuma execução no banco.");
            }
            for (Long id : ids) {
                Resultado r = svc.auditar(chain, id.longValue());
                System.out.println(r.descricao());
                if (r.status != Status.INTEGRO) {
                    codigo = 2;
                }
            }
        } catch (Exception e) {
            System.err.println("Erro na auditoria: " + e.getMessage());
            codigo = 3;
        } finally {
            if (chain != null) {
                chain.encerrar();
            }
        }
        System.exit(codigo);
    }
}
