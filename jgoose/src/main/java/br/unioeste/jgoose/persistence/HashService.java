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

    /**
     * Formatos canônicos. Cada execução guarda em execucao_mapeamento.formato_hash o formato
     * com que foi registrada, e a auditoria recalcula com esse mesmo formato. Assim, mudar o
     * formato NÃO invalida hashes já gravados na blockchain.
     *
     * V1: formato original (só horizontal). Execuções antigas têm formato_hash NULL = V1.
     * V2: inclui o tipo (HORIZONTAL/VERTICAL) no cabeçalho e, em cada relação, o modelo do
     *     elemento de origem e o modelo de destino. Necessário na vertical, onde um elemento
     *     BPMN/i* e um caso de uso podem ter o MESMO código.
     */
    public static final String FORMATO_V1 = "JGOOSE-TRACE-V1";
    public static final String FORMATO_V2 = "JGOOSE-TRACE-V2";
    public static final String FORMATO_ATUAL = FORMATO_V2;

    /** Mantido por compatibilidade com código que ainda referencie a constante antiga. */
    public static final String VERSAO_FORMATO = FORMATO_V1;

    public String calcularHashExecucao(long execucaoId) throws SQLException {
        try (Connection conn = DatabaseConnection.connect()) {
            return calcularHashExecucao(conn, execucaoId);
        }
    }

    public String calcularHashExecucao(Connection conn, long execucaoId) throws SQLException {
        String nome, origem, dataHora, tipo, formato;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT p.nome, e.origem, e.data_hora, e.tipo_rastreabilidade, e.formato_hash " +
                "FROM execucao_mapeamento e JOIN projeto p ON p.id = e.projeto_id WHERE e.id = ?")) {
            ps.setLong(1, execucaoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Execução inexistente: " + execucaoId);
                }
                nome = rs.getString(1);
                origem = rs.getString(2);
                dataHora = rs.getString(3);
                tipo = rs.getString(4);
                formato = rs.getString(5);
            }
        }
        if (formato == null || formato.trim().isEmpty()) {
            formato = FORMATO_V1;
        }
        boolean v2 = FORMATO_V2.equals(formato);
        if (!v2 && !FORMATO_V1.equals(formato)) {
            throw new IllegalStateException("Formato de hash desconhecido na execução " + execucaoId + ": " + formato);
        }

        String cabecalho = "X|" + esc(nome) + "|" + esc(origem) + "|" + esc(dataHora);
        if (v2) {
            cabecalho += "|" + esc(tipo);
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

        if (v2) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT e.classe, e.modelo, e.code, r.modelo_destino, r.codigo_destino, r.grau " +
                    "FROM relacionamento_rastreabilidade r " +
                    "JOIN elemento_rastreado e ON e.id = r.elemento_origem_id " +
                    "WHERE e.execucao_id = ?")) {
                ps.setLong(1, execucaoId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        linhas.add("R|" + esc(rs.getString(1)) + "|" + esc(rs.getString(2)) + "|" + esc(rs.getString(3))
                                + "|" + esc(rs.getString(4)) + "|" + esc(rs.getString(5)) + "|" + esc(rs.getString(6)));
                    }
                }
            }
        } else {
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
        }

        Collections.sort(linhas);

        StringBuilder sb = new StringBuilder();
        sb.append(formato).append('\n').append(cabecalho).append('\n');
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
