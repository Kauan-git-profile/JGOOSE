package br.unioeste.jgoose.persistence;

import br.unioeste.jgoose.model.TokensTraceability;
import br.unioeste.jgoose.model.TracedElement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class RastreabilidadeDAO {

    public static final String HORIZONTAL = "HORIZONTAL";
    public static final String VERTICAL = "VERTICAL";

    /** Mantido por compatibilidade: equivale a {@link #salvarHorizontal}. */
    public long salvar(TokensTraceability tokens, String origem, String nomeProjeto) {
        return salvarHorizontal(tokens, origem, nomeProjeto);
    }

    /**
     * Rastreabilidade horizontal: todos os elementos e relações pertencem ao mesmo modelo,
     * então o modelo de destino de cada relação é o próprio modelo do elemento de origem.
     */
    public long salvarHorizontal(TokensTraceability tokens, String origem, String nomeProjeto) {
        return salvar(tokens, origem, nomeProjeto, HORIZONTAL, null, null);
    }

    /**
     * Rastreabilidade vertical: liga elementos de um modelo-fonte (BPMN ou i*) aos elementos
     * do modelo-alvo (Use Case). Na JGOOSE os códigos dos dois lados costumam ser IGUAIS
     * (o caso de uso herda o código da atividade/elemento que o originou), por isso cada
     * relação grava também o modelo de destino - sem ele, "código 5" seria ambíguo.
     *
     * @param modeloFonte valor de TracedElement.getModel() do lado fonte (ex.: "BPMN", "I *")
     * @param modeloAlvo  valor de TracedElement.getModel() do lado alvo (ex.: "Use Case")
     */
    public long salvarVertical(TokensTraceability tokens, String origem, String nomeProjeto,
                               String modeloFonte, String modeloAlvo) {
        return salvar(tokens, origem, nomeProjeto, VERTICAL, modeloFonte, modeloAlvo);
    }

    private long salvar(TokensTraceability tokens, String origem, String nomeProjeto,
                        String tipo, String modeloFonte, String modeloAlvo) {
        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);
            try {
                long projetoId = obterOuCriarProjeto(conn, nomeProjeto);
                long execucaoId = criarExecucao(conn, projetoId, origem, tipo);

                // Escopo do TCC: Stakeholder x Requisitos, Requisitos x Stakeholder e Requisitos x Requisitos
                salvarLista(conn, execucaoId, tokens.getStakeholders(), tipo, modeloFonte, modeloAlvo);
                salvarLista(conn, execucaoId, tokens.getRequisitos(), tipo, modeloFonte, modeloAlvo);

                conn.commit();
                return execucaoId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private long obterOuCriarProjeto(Connection conn, String nome) throws SQLException {
        try (PreparedStatement psInsert = conn.prepareStatement(
                "INSERT OR IGNORE INTO projeto(nome) VALUES (?)")) {
            psInsert.setString(1, nome);
            psInsert.executeUpdate();
        }
        try (PreparedStatement psSelect = conn.prepareStatement(
                "SELECT id FROM projeto WHERE nome = ?")) {
            psSelect.setString(1, nome);
            try (ResultSet rs = psSelect.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private long criarExecucao(Connection conn, long projetoId, String origem, String tipo) throws SQLException {
        String sql = "INSERT INTO execucao_mapeamento(projeto_id, origem, tipo_rastreabilidade, formato_hash) "
                   + "VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, projetoId);
            ps.setString(2, origem);
            ps.setString(3, tipo);
            ps.setString(4, HashService.FORMATO_ATUAL);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private void salvarLista(Connection conn, long execucaoId, List<TracedElement> elementos,
                             String tipo, String modeloFonte, String modeloAlvo) throws SQLException {
        if (elementos == null) {
            return;
        }
        String insertElemento =
            "INSERT INTO elemento_rastreado(execucao_id, code, label, classe, fase, abreviacao, modelo) " +
            "VALUES (?,?,?,?,?,?,?)";
        String insertRelacionamento =
            "INSERT INTO relacionamento_rastreabilidade(elemento_origem_id, codigo_destino, grau, modelo_destino) " +
            "VALUES (?,?,?,?)";

        try (PreparedStatement psElem = conn.prepareStatement(insertElemento, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psRel = conn.prepareStatement(insertRelacionamento)) {

            for (TracedElement el : elementos) {
                String modelo = modeloDoElemento(el, tipo, modeloFonte);

                psElem.setLong(1, execucaoId);
                psElem.setString(2, el.getCode());
                psElem.setString(3, el.getLabel());
                psElem.setString(4, el.getClasse());
                psElem.setString(5, el.getFase());
                psElem.setString(6, el.getAbreviacao());
                psElem.setString(7, modelo);
                psElem.executeUpdate();

                long elementoId;
                try (ResultSet rs = psElem.getGeneratedKeys()) {
                    rs.next();
                    elementoId = rs.getLong(1);
                }

                if (el.getListConcflicts() != null) {
                    String modeloDestino = modeloDeDestino(modelo, tipo, modeloFonte, modeloAlvo);
                    for (String[] relacao : el.getListConcflicts()) {
                        psRel.setLong(1, elementoId);
                        psRel.setString(2, relacao[0]);
                        psRel.setString(3, relacao[1]);
                        psRel.setString(4, modeloDestino);
                        psRel.addBatch();
                    }
                }
            }
            psRel.executeBatch();
        }
    }

    /**
     * Alguns elementos da vertical i* (atores i* vindos das dependências) são criados sem modelo;
     * como só o lado alvo (Use Case) sempre o define, o que vier vazio pertence ao modelo-fonte.
     */
    private static String modeloDoElemento(TracedElement el, String tipo, String modeloFonte) {
        String m = el.getModel();
        if ((m == null || m.trim().isEmpty()) && VERTICAL.equals(tipo)) {
            return modeloFonte;
        }
        return m;
    }

    /** Horizontal: mesmo modelo. Vertical: o "outro lado" do par fonte/alvo. */
    static String modeloDeDestino(String modeloOrigem, String tipo, String modeloFonte, String modeloAlvo) {
        if (!VERTICAL.equals(tipo)) {
            return modeloOrigem;
        }
        return (modeloAlvo != null && modeloAlvo.equals(modeloOrigem)) ? modeloFonte : modeloAlvo;
    }

    /** Grava o hash SHA-256 calculado para a execução. */
    public void salvarHash(long execucaoId, String hashSha256) throws SQLException {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE execucao_mapeamento SET hash_sha256 = ? WHERE id = ?")) {
            ps.setString(1, hashSha256);
            ps.setLong(2, execucaoId);
            ps.executeUpdate();
        }
    }

    /** Grava o hash da transação on-chain que registrou o hash desta execução. */
    public void salvarTxHash(long execucaoId, String txHash) throws SQLException {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE execucao_mapeamento SET tx_hash = ? WHERE id = ?")) {
            ps.setString(1, txHash);
            ps.setLong(2, execucaoId);
            ps.executeUpdate();
        }
    }
}
