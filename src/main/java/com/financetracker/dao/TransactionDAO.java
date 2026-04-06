package com.financetracker.dao;

import com.financetracker.model.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private final Connection conn;

    public TransactionDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Transaction> findAll() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT * FROM transactions ORDER BY date DESC");
        while (rs.next()) list.add(map(rs));
        return list;
    }

    public List<Transaction> findByMonth(String month) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM transactions WHERE date LIKE ? ORDER BY date DESC");
        ps.setString(1, month + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    public List<Transaction> findByMonthAndCategory(String month, String category) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM transactions WHERE date LIKE ? AND category = ? ORDER BY date DESC");
        ps.setString(1, month + "%");
        ps.setString(2, category);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    public List<Transaction> findByCategory(String category) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM transactions WHERE category = ? ORDER BY date DESC");
        ps.setString(1, category);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    public void insert(Transaction t) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO transactions (date, name, category, amount, note) VALUES (?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, t.getDate().toString());
        ps.setString(2, t.getName());
        ps.setString(3, t.getCategory());
        ps.setDouble(4, t.getAmount());
        ps.setString(5, t.getNote() != null ? t.getNote() : "");
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) t.setId(keys.getInt(1));
    }

    public void update(Transaction t) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE transactions SET date=?, name=?, category=?, amount=?, note=? WHERE id=?");
        ps.setString(1, t.getDate().toString());
        ps.setString(2, t.getName());
        ps.setString(3, t.getCategory());
        ps.setDouble(4, t.getAmount());
        ps.setString(5, t.getNote() != null ? t.getNote() : "");
        ps.setInt(6, t.getId());
        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM transactions WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    private Transaction map(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getInt("id"),
            LocalDate.parse(rs.getString("date")),
            rs.getString("name"),
            rs.getString("category"),
            rs.getDouble("amount"),
            rs.getString("note")
        );
    }
}
