package br.unioeste.jgoose.persistence;

import br.unioeste.jgoose.model.TokensTraceability;
import br.unioeste.jgoose.model.TracedElement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RastreabilidadeDAO {

    public static final String HORIZONTAL = "HORIZONTAL";
    public static final String VERTICAL = "VERTICAL";

    /**
     * Nome usado quando o diagrama ainda não foi salvo em arquivo. Ele NÃO identifica um projeto:
     * diagramas diferentes sem arquivo compartilham esse nome, então nunca são sobrescritos.
     */
    public static final String SEM_TITULO = "Sem título";

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

                // Mesmo projeto (mesmo nome) e mesma origem = a MESMA execução: ela passa a refletir o
                // estado atual do diagrama (uma nova versão), em vez de acumular execuções duplicadas.
                // O histórico das versões fica na blockchain (o contrato guarda todas as versões).
                long execucaoId = SEM_TITULO.equals(nomeProjeto) ? -1 : buscarExecucao(conn, projetoId, origem);
                boolean reaproveitada = execucaoId > 0;
                String hashAnterior = null;
                Map<String, String> abreviacoes = null;
                if (reaproveitada) {
                    hashAnterior = lerHash(conn, execucaoId);
                    abreviacoes = lerAbreviacoes(conn, execucaoId);
                    limparDadosDaExecucao(conn, execucaoId);
                    atualizarTipoEFormato(conn, execucaoId, tipo);
                } else {
                    execucaoId = criarExecucao(conn, projetoId, origem, tipo);
                }

                // Todas as listas de TokensTraceability. A horizontal usa principalmente stakeholders e
                // requisitos; a vertical (BPMN/i* -> casos de uso) também usa ator do sistema, informação
                // organizacional, informação externa e objetivo do sistema (ver TraceIStarVertical /
                // TraceBPMNVertical). Sem isso, a maior parte do que a vertical gera não era persistida
                // nem, portanto, auditável.
                salvarLista(conn, execucaoId, tokens.getStakeholders(), tipo, modeloFonte, modeloAlvo, abreviacoes);
                salvarLista(conn, execucaoId, tokens.getRequisitos(), tipo, modeloFonte, modeloAlvo, abreviacoes);
                salvarLista(conn, execucaoId, tokens.getAtorSistema(), tipo, modeloFonte, modeloAlvo, abreviacoes);
                salvarLista(conn, execucaoId, tokens.getInformacaoOrg(), tipo, modeloFonte, modeloAlvo, abreviacoes);
                salvarLista(conn, execucaoId, tokens.getInformcaoExterna(), tipo, modeloFonte, modeloAlvo, abreviacoes);
                salvarLista(conn, execucaoId, tokens.getObjetivoSistema(), tipo, modeloFonte, modeloAlvo, abreviacoes);

                if (reaproveitada) {
                    // Compara o conteúdo novo com o anterior (mesma data_hora). Se mudou, é uma nova versão:
                    // atualiza a data e zera hash/txHash, que serão recalculados e registrados em seguida.
                    String hashNovo = new HashService().calcularHashExecucao(conn, execucaoId);
                    if (hashAnterior == null || !hashNovo.equals(hashAnterior)) {
                        marcarComoNovaVersao(conn, execucaoId);
                    }
                }

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

    /** Execução mais recente do mesmo projeto e da mesma origem, ou -1 se não existe. */
    private long buscarExecucao(Connection conn, long projetoId, String origem) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM execucao_mapeamento WHERE projeto_id = ? AND origem = ? ORDER BY id DESC LIMIT 1")) {
            ps.setLong(1, projetoId);
            ps.setString(2, origem);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : -1;
            }
        }
    }

    private String lerHash(Connection conn, long execucaoId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT hash_sha256 FROM execucao_mapeamento WHERE id = ?")) {
            ps.setLong(1, execucaoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /** Abreviações já gravadas para a execução, indexadas por classe, modelo e código do elemento. */
    private Map<String, String> lerAbreviacoes(Connection conn, long execucaoId) throws SQLException {
        Map<String, String> mapa = new HashMap<String, String>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT classe, modelo, code, abreviacao FROM elemento_rastreado WHERE execucao_id = ?")) {
            ps.setLong(1, execucaoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mapa.put(chaveAbreviacao(rs.getString(1), rs.getString(2), rs.getString(3)), rs.getString(4));
                }
            }
        }
        return mapa;
    }

    private static String chaveAbreviacao(String classe, String modelo, String code) {
        return classe + "|" + modelo + "|" + code;
    }

    /**
     * As abreviações ([STK 0], [RF 3]...) vêm de contadores estáticos das classes Traced*, que continuam
     * crescendo a cada geração na mesma sessão. Como entram no hash, gerar de novo um diagrama IDÊNTICO
     * produziria um hash diferente. Ao atualizar uma execução, o mesmo elemento (classe, modelo e código)
     * mantém a abreviação já gravada; assim só uma alteração real do conteúdo cria uma nova versão.
     */
    private static String abreviacaoParaGravar(TracedElement el, String modelo, Map<String, String> anteriores) {
        if (anteriores != null) {
            String guardada = anteriores.get(chaveAbreviacao(el.getClasse(), modelo, el.getCode()));
            if (guardada != null) {
                return guardada;
            }
        }
        return el.getAbreviacao();
    }

    /** Apaga os elementos e vínculos da execução (o estado anterior), para gravar o estado atual. */
    private void limparDadosDaExecucao(Connection conn, long execucaoId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM relacionamento_rastreabilidade WHERE elemento_origem_id IN "
              + "(SELECT id FROM elemento_rastreado WHERE execucao_id = ?)")) {
            ps.setLong(1, execucaoId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM elemento_rastreado WHERE execucao_id = ?")) {
            ps.setLong(1, execucaoId);
            ps.executeUpdate();
        }
    }

    /** Execuções criadas por versões antigas (sem tipo/formato) passam a usar o formato atual do hash. */
    private void atualizarTipoEFormato(Connection conn, long execucaoId, String tipo) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE execucao_mapeamento SET tipo_rastreabilidade = ?, formato_hash = ? WHERE id = ?")) {
            ps.setString(1, tipo);
            ps.setString(2, HashService.FORMATO_ATUAL);
            ps.setLong(3, execucaoId);
            ps.executeUpdate();
        }
    }

    private void marcarComoNovaVersao(Connection conn, long execucaoId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE execucao_mapeamento SET data_hora = datetime('now'), hash_sha256 = NULL, tx_hash = NULL WHERE id = ?")) {
            ps.setLong(1, execucaoId);
            ps.executeUpdate();
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
                             String tipo, String modeloFonte, String modeloAlvo,
                             Map<String, String> abreviacoes) throws SQLException {
        if (elementos == null || elementos.isEmpty()) {
            return;
        }
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
                psElem.setString(6, abreviacaoParaGravar(el, modelo, abreviacoes));
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
