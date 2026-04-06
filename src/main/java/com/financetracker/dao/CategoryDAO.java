package com.financetracker.dao;

import com.financetracker.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private final Connection conn;

    public CategoryDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT * FROM categories ORDER BY name");
        while (rs.next()) {
            list.add(new Category(rs.getInt("id"), rs.getString("name"), rs.getString("color")));
        }
        return list;
    }

    public List<String> findAllNames() throws SQLException {
        List<String> names = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT name FROM categories ORDER BY name");
        while (rs.next()) names.add(rs.getString("name"));
        return names;
    }

    public void insert(Category c) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO categories (name, color) VALUES (?, ?)",
            Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, c.getName());
        ps.setString(2, c.getColor());
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) c.setId(keys.getInt(1));
    }

    public Category findByName(String name) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM categories WHERE name = ?");
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Category(rs.getInt("id"), rs.getString("name"), rs.getString("color"));
        }
        return null;
    }
}
