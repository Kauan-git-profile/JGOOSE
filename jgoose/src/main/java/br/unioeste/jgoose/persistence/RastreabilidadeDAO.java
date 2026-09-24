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

    
      @param tokens  
      @param origem  
                     
      @return
     
    public long salvar(TokensTraceability tokens, String origem) {
        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);

            long execucaoId = criarExecucao(conn, origem);

      
            salvarLista(conn, execucaoId, tokens.getStakeholders());
            salvarLista(conn, execucaoId, tokens.getRequisitos());

     

            conn.commit();
            return execucaoId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private long criarExecucao(Connection conn, String origem) throws SQLException {
        String sql = "INSERT INTO execucao_mapeamento(origem) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, origem);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private void salvarLista(Connection conn, long execucaoId, List<TracedElement> elementos) throws SQLException {
        String insertElemento =
            "INSERT INTO elemento_rastreado(execucao_id, code, label, classe, fase, abreviacao, modelo) " +
            "VALUES (?,?,?,?,?,?,?)";
        String insertRelacionamento =
            "INSERT INTO relacionamento_rastreabilidade(elemento_origem_id, codigo_destino, grau) VALUES (?,?,?)";

        try (PreparedStatement psElem = conn.prepareStatement(insertElemento, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psRel = conn.prepareStatement(insertRelacionamento)) {

            for (TracedElement el : elementos) {
                psElem.setLong(1, execucaoId);
                psElem.setString(2, el.getCode());
                psElem.setString(3, el.getLabel());
                psElem.setString(4, el.getClasse());
                psElem.setString(5, el.getFase());
                psElem.setString(6, el.getAbreviacao());
                psElem.setString(7, el.getModel());
                psElem.executeUpdate();

                long elementoId;
                try (ResultSet rs = psElem.getGeneratedKeys()) {
                    rs.next();
                    elementoId = rs.getLong(1);
                }

                if (el.getListConcflicts() != null) {
                    for (String[] relacao : el.getListConcflicts()) {
                        psRel.setLong(1, elementoId);
                        psRel.setString(2, relacao[0]); // código do elemento relacionado
                        psRel.setString(3, relacao[1]); // grau: A, M ou B
                        psRel.addBatch();
                    }
                }
            }
            psRel.executeBatch();
        }
    }
}
