package com.financetracker;

import com.financetracker.model.Budget;
import com.financetracker.model.Transaction;
import com.financetracker.service.FinanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {

    private FinanceService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new FinanceService();
    }

    @Test
    void testIsOverBudget_notOverBudget() throws Exception {
        String month = LocalDate.now().toString().substring(0, 7);
        // "Mat" has a seeded budget of 3000 kr
        // Spending here is 500 — under budget
        assertFalse(service.isOverBudget("Mat", month, 500.0));
    }

    @Test
    void testIsOverBudget_overBudget() throws Exception {
        String month = LocalDate.now().toString().substring(0, 7);
        // "Mat" has a seeded budget of 3000 kr
        // Spending here is 5000 — over budget
        assertTrue(service.isOverBudget("Mat", month, 5000.0));
    }

    @Test
    void testIsOverBudget_exactlyAtLimit() throws Exception {
        String month = LocalDate.now().toString().substring(0, 7);
        // Exactly at limit should NOT be over budget
        assertFalse(service.isOverBudget("Mat", month, 3000.0));
    }

    @Test
    void testIsOverBudget_nobudgetSet() throws Exception {
        // No budget for this category — should never be "over"
        assertFalse(service.isOverBudget("UnknownCategory", "2020-01", 9999.0));
    }

    @Test
    void testBudgetLimitAmountProperty() {
        Budget b = new Budget(1, 2, "2026-04", 1500.0);
        assertEquals(1500.0, b.getLimitAmount(), 0.01);
        b.setLimitAmount(2500.0);
        assertEquals(2500.0, b.getLimitAmount(), 0.01);
    }

    @Test
    void testCalculateExpensesForBudgetCheck() {
        List<Transaction> txns = List.of(
            new Transaction(1, LocalDate.now(), "Rema",    "Mat", -400.0, ""),
            new Transaction(2, LocalDate.now(), "Coop",    "Mat", -600.0, ""),
            new Transaction(3, LocalDate.now(), "Lønnutb", "Lønn", 35000.0, "") // should not count
        );
        double expenses = service.calculateExpenses(txns);
        assertEquals(1000.0, expenses, 0.01);
    }
}
