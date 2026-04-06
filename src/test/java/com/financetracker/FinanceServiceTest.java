package com.financetracker;

import com.financetracker.model.Transaction;
import com.financetracker.service.FinanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FinanceServiceTest {

    private FinanceService service;
    private List<Transaction> transactions;

    @BeforeEach
    void setUp() throws Exception {
        service = new FinanceService();
        transactions = List.of(
            new Transaction(1, LocalDate.now(), "Lønn",   "Lønn",          35000.0,  ""),
            new Transaction(2, LocalDate.now(), "Rema",   "Mat",           -1200.0,  ""),
            new Transaction(3, LocalDate.now(), "Ruter",  "Transport",      -380.0,  ""),
            new Transaction(4, LocalDate.now(), "Kino",   "Underholdning",  -180.0,  ""),
            new Transaction(5, LocalDate.now(), "Husleie","Bolig",         -9500.0,  "")
        );
    }

    @Test
    void testCalculateBalance() {
        double balance = service.calculateBalance(transactions);
        assertEquals(23740.0, balance, 0.01);
    }

    @Test
    void testCalculateIncome() {
        double income = service.calculateIncome(transactions);
        assertEquals(35000.0, income, 0.01);
    }

    @Test
    void testCalculateExpenses() {
        double expenses = service.calculateExpenses(transactions);
        assertEquals(11260.0, expenses, 0.01);
    }

    @Test
    void testCalculateBalanceEmpty() {
        double balance = service.calculateBalance(List.of());
        assertEquals(0.0, balance, 0.01);
    }

    @Test
    void testCalculateIncomeOnlyExpenses() {
        List<Transaction> expenseOnly = List.of(
            new Transaction(1, LocalDate.now(), "Rema", "Mat", -500.0, "")
        );
        assertEquals(0.0, service.calculateIncome(expenseOnly), 0.01);
    }

    @Test
    void testSpendingByCategory() {
        Map<String, Double> spending = service.spendingByCategory(transactions);
        assertEquals(1200.0,  spending.get("Mat"),           0.01);
        assertEquals(380.0,   spending.get("Transport"),     0.01);
        assertEquals(180.0,   spending.get("Underholdning"), 0.01);
        assertEquals(9500.0,  spending.get("Bolig"),         0.01);
        assertNull(spending.get("Lønn")); // income not included
    }

    @Test
    void testSpendingByCategoryEmpty() {
        Map<String, Double> spending = service.spendingByCategory(List.of());
        assertTrue(spending.isEmpty());
    }

    @Test
    void testIsIncomeAndIsExpense() {
        Transaction income  = new Transaction(1, LocalDate.now(), "Lønn", "Lønn", 1000.0, "");
        Transaction expense = new Transaction(2, LocalDate.now(), "Rema", "Mat", -500.0, "");
        assertTrue(income.isIncome());
        assertFalse(income.isExpense());
        assertTrue(expense.isExpense());
        assertFalse(expense.isIncome());
    }
}
