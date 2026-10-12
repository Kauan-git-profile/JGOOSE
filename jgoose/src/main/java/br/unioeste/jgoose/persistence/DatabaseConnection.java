package br.unioeste.jgoose.persistence;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Responsável pela conexão com o banco SQLite off-chain do JGOOSE
 * e pela criação do schema de rastreabilidade, caso ainda não exista.
 *
 * Este banco atua como o repositório off-chain descrito no TCC:
 * armazena as estruturas de rastreabilidade brutas extraídas da JGOOSE,
 * servindo de base para o cálculo do hash (SHA-256) que é enviado à camada on-chain.
 */
public class DatabaseConnection {

    private static final String ARQUIVO_PADRAO = "jgoose_rastreabilidade.db";

    /**
     * Caminho do arquivo .db. Usa a variável de ambiente JGOOSE_DB_PATH, se existir;
     * caso contrário, o arquivo padrão no diretório de trabalho (de onde o jar foi executado).
     */
    public static String caminhoDoBanco() {
        String v = System.getenv("JGOOSE_DB_PATH");
        return (v == null || v.trim().isEmpty()) ? ARQUIVO_PADRAO : v.trim();
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + caminhoDoBanco());
    }

    public static void criarSchema() {

        System.out.println("[SQLite] Caminho do banco: " + new File(caminhoDoBanco()).getAbsolutePath());

        String sqlProjeto =
            "CREATE TABLE IF NOT EXISTS projeto (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  nome TEXT NOT NULL UNIQUE" +
            ");";

        String sqlExecucao =
            "CREATE TABLE IF NOT EXISTS execucao_mapeamento (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  projeto_id INTEGER NOT NULL," +
            "  data_hora TEXT NOT NULL DEFAULT (datetime('now'))," +
            "  origem TEXT," +
            "  hash_sha256 TEXT," +          // SHA-256 dos dados desta execução (calculado após salvar)
            "  tx_hash TEXT," +              // hash da transação on-chain que registrou o hash acima
            "  FOREIGN KEY (projeto_id) REFERENCES projeto(id)" +
            ");";

        String sqlElemento =
            "CREATE TABLE IF NOT EXISTS elemento_rastreado (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  execucao_id INTEGER NOT NULL," +
            "  code TEXT," +
            "  label TEXT," +
            "  classe TEXT NOT NULL," +
            "  fase TEXT," +
            "  abreviacao TEXT," +
            "  modelo TEXT" +
            ");";

        String sqlRelacionamento =
            "CREATE TABLE IF NOT EXISTS relacionamento_rastreabilidade (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  elemento_origem_id INTEGER NOT NULL," +
            "  codigo_destino TEXT NOT NULL," +
            "  grau TEXT NOT NULL," +
            "  FOREIGN KEY (elemento_origem_id) REFERENCES elemento_rastreado(id)" +
            ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlProjeto);      // precisa vir antes de sqlExecucao (a FK depende dela)
            stmt.execute(sqlExecucao);
            stmt.execute(sqlElemento);
            stmt.execute(sqlRelacionamento);

            // Migração: bancos criados antes desta versão não têm a coluna tx_hash.
            garantirColuna(conn, "execucao_mapeamento", "tx_hash", "TEXT");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void garantirColuna(Connection conn, String tabela, String coluna, String tipo) throws SQLException {
        boolean existe = false;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + tabela + ")")) {
            while (rs.next()) {
                if (coluna.equalsIgnoreCase(rs.getString("name"))) {
                    existe = true;
                    break;
                }
            }
        }
        if (!existe) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE " + tabela + " ADD COLUMN " + coluna + " " + tipo);
            }
        }
    }
}
