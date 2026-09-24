package br.unioeste.jgoose.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Responsável pela conexão com o banco SQLite off-chain do JGOOSE
 * e pela criação do schema de rastreabilidade, caso ainda não exista.
 *
 * Este banco atua como o repositório off-chain descrito no TCC:
 * armazena as estruturas de rastreabilidade brutas extraídas da JGOOSE,
 * servindo de base para o cálculo posterior do hash (SHA-256) que será
 * enviado à camada on-chain.
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:jgoose_rastreabilidade.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void criarSchema() {

        System.out.println("[SQLite] Caminho do banco: " + new java.io.File("jgoose_rastreabilidade.db").getAbsolutePath());

        String sqlElemento =
            "CREATE TABLE IF NOT EXISTS elemento_rastreado (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  execucao_id INTEGER NOT NULL," +
            "  code TEXT," +
            "  label TEXT," +
            "  classe TEXT NOT NULL," +      // 'Requisitos', 'Stakeholder', etc.
            "  fase TEXT," +
            "  abreviacao TEXT," +
            "  modelo TEXT" +                // 'Use Case', 'BPMN', 'iStar'
            ");";

        String sqlRelacionamento =
            "CREATE TABLE IF NOT EXISTS relacionamento_rastreabilidade (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  elemento_origem_id INTEGER NOT NULL," +
            "  codigo_destino TEXT NOT NULL," +
            "  grau TEXT NOT NULL," +        // 'A' direto, 'M'/'B' indireto
            "  FOREIGN KEY (elemento_origem_id) REFERENCES elemento_rastreado(id)" +
            ");";

        String sqlExecucao =
            "CREATE TABLE IF NOT EXISTS execucao_mapeamento (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  data_hora TEXT NOT NULL DEFAULT (datetime('now'))," +
            "  origem TEXT," +                // ex.: 'UC Horizontal', 'BPMN Horizontal'
            "  hash_sha256 TEXT" +             // preenchido depois, pelo módulo de auditoria
            ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlExecucao);
            stmt.execute(sqlElemento);
            stmt.execute(sqlRelacionamento);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
