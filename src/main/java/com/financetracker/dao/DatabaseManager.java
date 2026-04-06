package com.financetracker.dao;

import java.sql.*;
import java.time.LocalDate;

public class DatabaseManager {
    private static final String DB_PATH = "finance.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        initSchema();
        seedIfEmpty();
    }

    public static DatabaseManager getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() { return connection; }

    private void initSchema() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("""
            CREATE TABLE IF NOT EXISTS categories (
                id    INTEGER PRIMARY KEY AUTOINCREMENT,
                name  TEXT NOT NULL UNIQUE,
                color TEXT NOT NULL DEFAULT '#607D8B'
            )
        """);
        st.execute("""
            CREATE TABLE IF NOT EXISTS transactions (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                date     TEXT NOT NULL,
                name     TEXT NOT NULL,
                category TEXT NOT NULL,
                amount   REAL NOT NULL,
                note     TEXT
            )
        """);
        st.execute("""
            CREATE TABLE IF NOT EXISTS budgets (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                category_id     INTEGER NOT NULL,
                month           TEXT NOT NULL,
                limit_amount    REAL NOT NULL,
                FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
                UNIQUE (category_id, month)
            )
        """);
    }

    private void seedIfEmpty() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM transactions");
        if (rs.getInt(1) > 0) return;

        // Seed categories
        String[][] cats = {
            {"Lønn", "#4CAF50"},
            {"Mat", "#FF5722"},
            {"Transport", "#2196F3"},
            {"Underholdning", "#9C27B0"},
            {"Helse", "#F44336"},
            {"Bolig", "#FF9800"},
            {"Klær", "#E91E63"},
            {"Annet", "#607D8B"}
        };
        PreparedStatement catPs = connection.prepareStatement(
            "INSERT OR IGNORE INTO categories (name, color) VALUES (?, ?)");
        for (String[] c : cats) {
            catPs.setString(1, c[0]);
            catPs.setString(2, c[1]);
            catPs.execute();
        }

        // Seed transactions (last 6 months)
        LocalDate now = LocalDate.now();
        Object[][] txns = {
            {now.minusMonths(5).withDayOfMonth(25), "Lønnsutbetaling", "Lønn", 35000.0, ""},
            {now.minusMonths(5).withDayOfMonth(3),  "Rema 1000",       "Mat",  -1200.0, "Ukeshandel"},
            {now.minusMonths(5).withDayOfMonth(10), "Ruter månedskort","Transport", -380.0, ""},
            {now.minusMonths(5).withDayOfMonth(15), "Kino",            "Underholdning", -180.0, ""},
            {now.minusMonths(4).withDayOfMonth(25), "Lønnsutbetaling", "Lønn", 35000.0, ""},
            {now.minusMonths(4).withDayOfMonth(4),  "Coop Extra",      "Mat",  -950.0, ""},
            {now.minusMonths(4).withDayOfMonth(8),  "Ruter månedskort","Transport", -380.0, ""},
            {now.minusMonths(4).withDayOfMonth(20), "Lege",            "Helse", -250.0, "Egenandel"},
            {now.minusMonths(4).withDayOfMonth(22), "H&M",             "Klær",  -650.0, ""},
            {now.minusMonths(3).withDayOfMonth(25), "Lønnsutbetaling", "Lønn", 35000.0, ""},
            {now.minusMonths(3).withDayOfMonth(2),  "Meny",            "Mat",  -1400.0, ""},
            {now.minusMonths(3).withDayOfMonth(9),  "Ruter månedskort","Transport", -380.0, ""},
            {now.minusMonths(3).withDayOfMonth(14), "Spotify",         "Underholdning", -109.0, ""},
            {now.minusMonths(3).withDayOfMonth(18), "Husleie",         "Bolig", -9500.0, ""},
            {now.minusMonths(2).withDayOfMonth(25), "Lønnsutbetaling", "Lønn", 35000.0, ""},
            {now.minusMonths(2).withDayOfMonth(5),  "Kiwi",            "Mat",  -800.0, ""},
            {now.minusMonths(2).withDayOfMonth(9),  "Ruter månedskort","Transport", -380.0, ""},
            {now.minusMonths(2).withDayOfMonth(12), "Apotek",          "Helse", -180.0, ""},
            {now.minusMonths(2).withDayOfMonth(18), "Husleie",         "Bolig", -9500.0, ""},
            {now.minusMonths(2).withDayOfMonth(26), "Netflix",         "Underholdning", -149.0, ""},
            {now.minusMonths(1).withDayOfMonth(25), "Lønnsutbetaling", "Lønn", 35000.0, ""},
            {now.minusMonths(1).withDayOfMonth(3),  "Rema 1000",       "Mat",  -1100.0, ""},
            {now.minusMonths(1).withDayOfMonth(9),  "Ruter månedskort","Transport", -380.0, ""},
            {now.minusMonths(1).withDayOfMonth(10), "Zara",            "Klær",  -799.0, "Jakke"},
            {now.minusMonths(1).withDayOfMonth(18), "Husleie",         "Bolig", -9500.0, ""},
            {now.minusMonths(1).withDayOfMonth(22), "Tannlege",        "Helse", -800.0, ""},
            {now.withDayOfMonth(1),                 "Husleie",         "Bolig", -9500.0, ""},
            {now.withDayOfMonth(2),                 "Coop Extra",      "Mat",  -670.0, ""},
            {now.withDayOfMonth(5),                 "Ruter månedskort","Transport", -380.0, ""},
        };

        PreparedStatement txPs = connection.prepareStatement(
            "INSERT INTO transactions (date, name, category, amount, note) VALUES (?, ?, ?, ?, ?)");
        for (Object[] t : txns) {
            txPs.setString(1, t[0].toString());
            txPs.setString(2, (String) t[1]);
            txPs.setString(3, (String) t[2]);
            txPs.setDouble(4, (Double) t[3]);
            txPs.setString(5, (String) t[4]);
            txPs.execute();
        }

        // Seed budgets for current month
        String currentMonth = now.toString().substring(0, 7);
        ResultSet catRs = connection.createStatement().executeQuery("SELECT id, name FROM categories");
        PreparedStatement budgetPs = connection.prepareStatement(
            "INSERT OR IGNORE INTO budgets (category_id, month, limit_amount) VALUES (?, ?, ?)");
        while (catRs.next()) {
            int catId = catRs.getInt("id");
            String catName = catRs.getString("name");
            double limit = switch (catName) {
                case "Mat" -> 3000.0;
                case "Transport" -> 600.0;
                case "Underholdning" -> 500.0;
                case "Helse" -> 1000.0;
                case "Bolig" -> 10000.0;
                case "Klær" -> 1500.0;
                default -> 2000.0;
            };
            budgetPs.setInt(1, catId);
            budgetPs.setString(2, currentMonth);
            budgetPs.setDouble(3, limit);
            budgetPs.execute();
        }
    }
}
