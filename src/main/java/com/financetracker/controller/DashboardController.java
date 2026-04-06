package com.financetracker.controller;

import com.financetracker.model.Transaction;
import com.financetracker.service.FinanceService;
import com.financetracker.util.CurrencyFormatter;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardController {

    @FXML private Label bannerGreeting;
    @FXML private Label bannerSubtitle;
    @FXML private Label bannerDate;

    @FXML private Label labelBalance;
    @FXML private Label labelBalanceChange;
    @FXML private Label labelIncome;
    @FXML private Label labelIncomeChange;
    @FXML private Label labelExpenses;
    @FXML private Label labelExpensesChange;
    @FXML private Label labelSavingsRate;
    @FXML private Label labelSavingsAmount;

    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis barChartXAxis;
    @FXML private NumberAxis barChartYAxis;
    @FXML private PieChart pieChart;

    @FXML private VBox recentTxList;
    @FXML private VBox monthlyOverviewList;

    private FinanceService financeService;
    private static final Locale NB = new Locale("nb", "NO");

    @FXML
    public void initialize() {
        try {
            financeService = new FinanceService();
            loadBanner();
            loadMetrics();
            loadBarChart();
            loadPieChart();
            loadRecentTransactions();
            loadMonthlyOverview();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Banner ─────────────────────────────────────────────────────────────────

    private void loadBanner() {
        int hour = LocalTime.now().getHour();
        String greeting = hour < 10 ? "God morgen" : hour < 17 ? "God dag" : "God kveld";
        bannerGreeting.setText(greeting);

        YearMonth now = YearMonth.now();
        String monthName = now.getMonth().getDisplayName(TextStyle.FULL, NB);
        String cap = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        bannerDate.setText(cap + " " + now.getYear());
        bannerSubtitle.setText("Saldo og aktivitet for " + cap.toLowerCase() + " " + now.getYear());
    }

    // ── Metric Cards ───────────────────────────────────────────────────────────

    private void loadMetrics() throws SQLException {
        YearMonth now  = YearMonth.now();
        YearMonth prev = now.minusMonths(1);

        List<Transaction> all       = financeService.getAllTransactions();
        List<Transaction> thisMonth = financeService.getTransactionsByMonth(now.toString());
        List<Transaction> lastMonth = financeService.getTransactionsByMonth(prev.toString());

        double balance  = financeService.calculateBalance(all);
        double income   = financeService.calculateIncome(thisMonth);
        double expenses = financeService.calculateExpenses(thisMonth);
        double savings  = income - expenses;
        double rate     = income > 0 ? (savings / income) * 100 : 0;

        double lastIncome   = financeService.calculateIncome(lastMonth);
        double lastExpenses = financeService.calculateExpenses(lastMonth);

        labelBalance.setText(CurrencyFormatter.format(balance));
        labelBalanceChange.setText("Akkumulert total saldo");

        labelIncome.setText(CurrencyFormatter.format(income));
        labelIncomeChange.setText(changeLine(income, lastIncome));

        labelExpenses.setText(CurrencyFormatter.format(expenses));
        labelExpensesChange.setText(changeLine(expenses, lastExpenses));

        labelSavingsRate.setText(String.format("%.1f pst", rate));
        String savSign = savings >= 0 ? "+" : "";
        labelSavingsAmount.setText("Spart " + savSign + CurrencyFormatter.format(savings));
    }

    private String changeLine(double current, double previous) {
        if (previous == 0) return "Ingen data forrige måned";
        double pct = ((current - previous) / previous) * 100;
        String arrow = pct >= 0 ? "▲" : "▼";
        return String.format("%s %.1f %% fra forrige måned", arrow, Math.abs(pct));
    }

    // ── Bar Chart ──────────────────────────────────────────────────────────────

    private void loadBarChart() throws SQLException {
        Map<String, double[]> data = financeService.monthlyIncomeExpenses(6);

        XYChart.Series<String, Number> incomeSeries  = new XYChart.Series<>();
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        incomeSeries.setName("Inntekt");
        expenseSeries.setName("Utgifter");

        for (Map.Entry<String, double[]> e : data.entrySet()) {
            YearMonth ym  = YearMonth.parse(e.getKey());
            String label  = ym.getMonth().getDisplayName(TextStyle.SHORT, NB);
            incomeSeries.getData() .add(new XYChart.Data<>(label, e.getValue()[0]));
            expenseSeries.getData().add(new XYChart.Data<>(label, e.getValue()[1]));
        }

        barChart.getData().setAll(incomeSeries, expenseSeries);
    }

    // ── Pie Chart ──────────────────────────────────────────────────────────────

    private void loadPieChart() throws SQLException {
        String month = YearMonth.now().toString();
        List<Transaction> txns = financeService.getTransactionsByMonth(month);
        Map<String, Double> by = financeService.spendingByCategory(txns);

        pieChart.getData().clear();
        for (Map.Entry<String, Double> e : by.entrySet()) {
            PieChart.Data slice = new PieChart.Data(e.getKey(), e.getValue());
            pieChart.getData().add(slice);
        }
        for (PieChart.Data d : pieChart.getData()) {
            Tooltip tip = new Tooltip(d.getName() + "\n" + CurrencyFormatter.format(d.getPieValue()));
            Tooltip.install(d.getNode(), tip);
        }
    }

    // ── Recent Transactions ────────────────────────────────────────────────────

    private void loadRecentTransactions() throws SQLException {
        List<Transaction> all = financeService.getAllTransactions();
        recentTxList.getChildren().clear();

        int count = Math.min(7, all.size());
        for (int i = 0; i < count; i++) {
            Transaction t = all.get(i);

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("recent-tx-row");

            // Category color dot
            javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(4);
            dot.setStyle("-fx-fill: " + categoryColor(t.getCategory()) + ";");

            VBox nameBox = new VBox(1);
            Label nameLbl = new Label(t.getName());
            nameLbl.getStyleClass().add("recent-tx-name");
            Label catLbl  = new Label(t.getCategory());
            catLbl.getStyleClass().add("recent-tx-cat");
            nameBox.getChildren().addAll(nameLbl, catLbl);
            HBox.setHgrow(nameBox, Priority.ALWAYS);

            Label dateLbl = new Label(t.getDate().toString());
            dateLbl.getStyleClass().add("recent-tx-date");

            Label amtLbl = new Label(CurrencyFormatter.formatSigned(t.getAmount()));
            amtLbl.getStyleClass().add(t.isIncome() ? "recent-tx-positive" : "recent-tx-negative");

            row.getChildren().addAll(dot, nameBox, dateLbl, amtLbl);
            recentTxList.getChildren().add(row);
        }
    }

    // ── Monthly Overview ───────────────────────────────────────────────────────

    private void loadMonthlyOverview() throws SQLException {
        Map<String, double[]> data = financeService.monthlyIncomeExpenses(6);
        monthlyOverviewList.getChildren().clear();

        for (Map.Entry<String, double[]> e : data.entrySet()) {
            YearMonth ym  = YearMonth.parse(e.getKey());
            double income = e.getValue()[0];
            double exp    = e.getValue()[1];
            double saving = income - exp;

            HBox row = new HBox(4);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("month-row");

            String monthName = ym.getMonth().getDisplayName(TextStyle.SHORT, NB)
                + " " + ym.getYear();
            Label mLbl = new Label(monthName);
            mLbl.getStyleClass().add("month-row-label");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label iLbl = new Label(CurrencyFormatter.format(income));
            iLbl.getStyleClass().add("month-income-label");

            Label xLbl = new Label(CurrencyFormatter.format(exp));
            xLbl.getStyleClass().add("month-expense-label");

            Label sLbl = new Label((saving >= 0 ? "+" : "") + CurrencyFormatter.format(saving));
            sLbl.getStyleClass().addAll("month-savings-label",
                saving >= 0 ? "savings-positive" : "savings-negative");
            sLbl.setMinWidth(100);
            sLbl.setStyle("-fx-alignment:CENTER-RIGHT;");

            row.getChildren().addAll(mLbl, spacer, iLbl, xLbl, sLbl);
            monthlyOverviewList.getChildren().add(row);
        }
    }

    private String categoryColor(String cat) {
        return switch (cat) {
            case "Lønn"          -> "#10B981";
            case "Mat"           -> "#F97316";
            case "Transport"     -> "#3B82F6";
            case "Underholdning" -> "#8B5CF6";
            case "Helse"         -> "#EF4444";
            case "Bolig"         -> "#F59E0B";
            case "Klær"          -> "#EC4899";
            default              -> "#6B7280";
        };
    }
}
