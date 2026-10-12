package br.unioeste.jgoose.persistence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Calcula o hash SHA-256 do estado de UMA execução salva no banco.
 *
 * O hash é calculado sobre uma representação canônica (texto) dos dados:
 * as linhas são ordenadas e os campos escapados, então o resultado depende
 * apenas do CONTEÚDO (não da ordem de inserção nem dos ids internos).
 * Qualquer alteração em um elemento, em uma relação ou no cabeçalho da execução
 * muda o hash. O mesmo cálculo é usado na gravação e na auditoria.
 */
public class HashService {

    /** Versão do formato canônico. Se o formato mudar, mude também esta versão. */
    public static final String VERSAO_FORMATO = "JGOOSE-TRACE-V1";

    public String calcularHashExecucao(long execucaoId) throws SQLException {
        try (Connection conn = DatabaseConnection.connect()) {
            return calcularHashExecucao(conn, execucaoId);
        }
    }

    public String calcularHashExecucao(Connection conn, long execucaoId) throws SQLException {
        String cabecalho;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT p.nome, e.origem, e.data_hora " +
                "FROM execucao_mapeamento e JOIN projeto p ON p.id = e.projeto_id WHERE e.id = ?")) {
            ps.setLong(1, execucaoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Execução inexistente: " + execucaoId);
                }
                cabecalho = "X|" + esc(rs.getString(1)) + "|" + esc(rs.getString(2)) + "|" + esc(rs.getString(3));
            }
        }

        List<String> linhas = new ArrayList<String>();

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT classe, code, label, fase, abreviacao, modelo " +
                "FROM elemento_rastreado WHERE execucao_id = ?")) {
            ps.setLong(1, execucaoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    linhas.add("E|" + esc(rs.getString(1)) + "|" + esc(rs.getString(2)) + "|" + esc(rs.getString(3))
                            + "|" + esc(rs.getString(4)) + "|" + esc(rs.getString(5)) + "|" + esc(rs.getString(6)));
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT e.classe, e.code, r.codigo_destino, r.grau " +
                "FROM relacionamento_rastreabilidade r " +
                "JOIN elemento_rastreado e ON e.id = r.elemento_origem_id " +
                "WHERE e.execucao_id = ?")) {
            ps.setLong(1, execucaoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    linhas.add("R|" + esc(rs.getString(1)) + "|" + esc(rs.getString(2))
                            + "|" + esc(rs.getString(3)) + "|" + esc(rs.getString(4)));
                }
            }
        }

        Collections.sort(linhas);

        StringBuilder sb = new StringBuilder();
        sb.append(VERSAO_FORMATO).append('\n').append(cabecalho).append('\n');
        for (String l : linhas) {
            sb.append(l).append('\n');
        }
        return sha256Hex(sb.toString());
    }

    /** Escapa o separador e quebras de linha; null vira texto vazio. */
    static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n").replace("\r", "\\r");
    }

    static String sha256Hex(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(h.length * 2);
            for (byte b : h) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível nesta JVM", e);
        }
    }
}
