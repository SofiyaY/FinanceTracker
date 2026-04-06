package com.financetracker.model;

public class Budget {
    private int id;
    private int categoryId;
    private String categoryName;
    private String month; // YYYY-MM
    private double limitAmount;

    public Budget() {}

    public Budget(int id, int categoryId, String month, double limitAmount) {
        this.id = id;
        this.categoryId = categoryId;
        this.month = month;
        this.limitAmount = limitAmount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }
}
