package com.financetracker.controller;

import com.financetracker.service.FinanceService;
import com.financetracker.util.CurrencyFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

public class StatisticsController {

    @FXML private LineChart<String, Number> lineChart;
    @FXML private CategoryAxis lineXAxis;
    @FXML private NumberAxis lineYAxis;

    @FXML private TableView<Map.Entry<String, Map<String, Double>>> summaryTable;
    @FXML private TableColumn<Map.Entry<String, Map<String, Double>>, String> colMonth;

    private FinanceService financeService;
    private final int MONTHS = 6;

    @FXML
    public void initialize() {
        try {
            financeService = new FinanceService();
            loadLineChart();
            loadSummaryTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadLineChart() throws SQLException {
        Map<String, Double> balances = financeService.monthlyBalances(MONTHS);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Balanse");

        Locale norwegian = new Locale("nb", "NO");
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            YearMonth ym = YearMonth.parse(entry.getKey());
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, norwegian)
                + " " + ym.getYear();
            series.getData().add(new XYChart.Data<>(label, entry.getValue()));
        }

        lineChart.getData().setAll(series);
        lineChart.setTitle("Balanse over tid");
    }

    private void loadSummaryTable() throws SQLException {
        Map<String, Map<String, Double>> data = financeService.categoryTotalsPerMonth(MONTHS);

        // Gather all categories across months
        Set<String> allCategories = new LinkedHashSet<>();
        for (Map<String, Double> monthly : data.values()) {
            allCategories.addAll(monthly.keySet());
        }

        // Month column
        colMonth.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getKey()));

        // Remove any old category columns
        summaryTable.getColumns().removeIf(col -> col != colMonth);

        // Add one column per category
        for (String cat : allCategories) {
            TableColumn<Map.Entry<String, Map<String, Double>>, String> col = new TableColumn<>(cat);
            col.setCellValueFactory(c -> {
                Double val = c.getValue().getValue().get(cat);
                return new SimpleStringProperty(val != null ? CurrencyFormatter.format(val) : "-");
            });
            col.setPrefWidth(130);
            summaryTable.getColumns().add(col);
        }

        // Set data
        List<Map.Entry<String, Map<String, Double>>> rows = new ArrayList<>(data.entrySet());
        summaryTable.getItems().setAll(rows);
    }
}
