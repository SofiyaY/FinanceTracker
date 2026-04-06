package com.financetracker.service;

import com.financetracker.dao.BudgetDAO;
import com.financetracker.dao.CategoryDAO;
import com.financetracker.dao.DatabaseManager;
import com.financetracker.dao.TransactionDAO;
import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class FinanceService {
    private final TransactionDAO transactionDAO;
    private final CategoryDAO categoryDAO;
    private final BudgetDAO budgetDAO;

    public FinanceService() throws SQLException {
        var conn = DatabaseManager.getInstance().getConnection();
        this.transactionDAO = new TransactionDAO(conn);
        this.categoryDAO = new CategoryDAO(conn);
        this.budgetDAO = new BudgetDAO(conn);
    }

    // ── Transactions ──────────────────────────────────────────────────────────

    public List<Transaction> getAllTransactions() throws SQLException {
        return transactionDAO.findAll();
    }

    public List<Transaction> getTransactionsByMonth(String month) throws SQLException {
        return transactionDAO.findByMonth(month);
    }

    public List<Transaction> getTransactionsByMonthAndCategory(String month, String category) throws SQLException {
        if (category == null || category.isEmpty()) return getTransactionsByMonth(month);
        return transactionDAO.findByMonthAndCategory(month, category);
    }

    public void addTransaction(Transaction t) throws SQLException {
        transactionDAO.insert(t);
    }

    public void updateTransaction(Transaction t) throws SQLException {
        transactionDAO.update(t);
    }

    public void deleteTransaction(int id) throws SQLException {
        transactionDAO.delete(id);
    }

    // ── Balance & Summaries ───────────────────────────────────────────────────

    public double calculateBalance(List<Transaction> transactions) {
        return transactions.stream().mapToDouble(Transaction::getAmount).sum();
    }

    public double calculateIncome(List<Transaction> transactions) {
        return transactions.stream()
            .filter(Transaction::isIncome)
            .mapToDouble(Transaction::getAmount)
            .sum();
    }

    public double calculateExpenses(List<Transaction> transactions) {
        return transactions.stream()
            .filter(Transaction::isExpense)
            .mapToDouble(t -> Math.abs(t.getAmount()))
            .sum();
    }

    /** Returns total spending per category for the given transactions. */
    public Map<String, Double> spendingByCategory(List<Transaction> transactions) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if (t.isExpense()) {
                map.merge(t.getCategory(), Math.abs(t.getAmount()), Double::sum);
            }
        }
        return map;
    }

    /**
     * Returns monthly balance snapshots for the last N months.
     * Key = "YYYY-MM", value = cumulative balance up to end of that month.
     */
    public Map<String, Double> monthlyBalances(int months) throws SQLException {
        List<Transaction> all = transactionDAO.findAll();
        Map<String, Double> result = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.from(now).minusMonths(i);
            String prefix = ym.toString();
            double balance = all.stream()
                .filter(t -> !t.getDate().isAfter(ym.atEndOfMonth()))
                .mapToDouble(Transaction::getAmount)
                .sum();
            result.put(prefix, balance);
        }
        return result;
    }

    /**
     * Returns income and expense totals per month for the last N months.
     * Returns a map with "YYYY-MM" -> double[]{income, expenses}
     */
    public Map<String, double[]> monthlyIncomeExpenses(int months) throws SQLException {
        Map<String, double[]> result = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.from(now).minusMonths(i);
            String month = ym.toString();
            List<Transaction> txns = transactionDAO.findByMonth(month);
            double income = calculateIncome(txns);
            double expenses = calculateExpenses(txns);
            result.put(month, new double[]{income, expenses});
        }
        return result;
    }

    /** Returns per-category totals for each month, for the Statistics view. */
    public Map<String, Map<String, Double>> categoryTotalsPerMonth(int months) throws SQLException {
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.from(now).minusMonths(i);
            String month = ym.toString();
            List<Transaction> txns = transactionDAO.findByMonth(month);
            result.put(month, spendingByCategory(txns));
        }
        return result;
    }

    // ── Categories ────────────────────────────────────────────────────────────

    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.findAll();
    }

    public List<String> getCategoryNames() throws SQLException {
        return categoryDAO.findAllNames();
    }

    // ── Budgets ───────────────────────────────────────────────────────────────

    public List<Budget> getBudgetsForMonth(String month) throws SQLException {
        return budgetDAO.findByMonth(month);
    }

    public void saveBudget(Budget b) throws SQLException {
        budgetDAO.upsert(b);
    }

    public void deleteBudget(int id) throws SQLException {
        budgetDAO.delete(id);
    }

    /**
     * Checks if a category is over budget for the given month.
     * Returns true if spent > limit, false if under or no budget set.
     */
    public boolean isOverBudget(String categoryName, String month, double spent) throws SQLException {
        List<Budget> budgets = budgetDAO.findByMonth(month);
        return budgets.stream()
            .filter(b -> b.getCategoryName().equals(categoryName))
            .anyMatch(b -> spent > b.getLimitAmount());
    }

    /**
     * Returns the spending amount for a category in a given month.
     */
    public double getSpentForCategory(String categoryName, String month) throws SQLException {
        List<Transaction> txns = transactionDAO.findByMonthAndCategory(month, categoryName);
        return calculateExpenses(txns);
    }
}
