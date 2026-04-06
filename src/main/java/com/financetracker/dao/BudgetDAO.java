package com.financetracker.dao;

import com.financetracker.model.Budget;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {
    private final Connection conn;

    public BudgetDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Budget> findByMonth(String month) throws SQLException {
        List<Budget> list = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement("""
            SELECT b.id, b.category_id, c.name AS category_name, b.month, b.limit_amount
            FROM budgets b
            JOIN categories c ON c.id = b.category_id
            WHERE b.month = ?
            ORDER BY c.name
        """);
        ps.setString(1, month);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Budget budget = new Budget(
                rs.getInt("id"),
                rs.getInt("category_id"),
                rs.getString("month"),
                rs.getDouble("limit_amount")
            );
            budget.setCategoryName(rs.getString("category_name"));
            list.add(budget);
        }
        return list;
    }

    public void upsert(Budget b) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO budgets (category_id, month, limit_amount)
            VALUES (?, ?, ?)
            ON CONFLICT(category_id, month) DO UPDATE SET limit_amount = excluded.limit_amount
        """);
        ps.setInt(1, b.getCategoryId());
        ps.setString(2, b.getMonth());
        ps.setDouble(3, b.getLimitAmount());
        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM budgets WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}
