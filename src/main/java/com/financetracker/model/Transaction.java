package com.financetracker.model;

import java.time.LocalDate;

public class Transaction {
    private int id;
    private LocalDate date;
    private String name;
    private String category;
    private double amount;
    private String note;

    public Transaction() {}

    public Transaction(int id, LocalDate date, String name, String category, double amount, String note) {
        this.id = id;
        this.date = date;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isIncome() { return amount > 0; }
    public boolean isExpense() { return amount < 0; }
}
