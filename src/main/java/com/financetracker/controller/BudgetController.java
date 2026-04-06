package com.financetracker.controller;

import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import com.financetracker.service.FinanceService;
import com.financetracker.util.CurrencyFormatter;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BudgetController {

    @FXML private ComboBox<String> monthPicker;
    @FXML private VBox budgetList;
    @FXML private Button btnAddBudget;

    private FinanceService financeService;

    @FXML
    public void initialize() {
        try {
            financeService = new FinanceService();
            setupMonthPicker();
            loadBudgets();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupMonthPicker() {
        YearMonth now = YearMonth.now();
        for (int i = 2; i >= -1; i--) {
            monthPicker.getItems().add(now.minusMonths(i).toString());
        }
        monthPicker.setValue(now.toString());
        monthPicker.setOnAction(e -> loadBudgets());
    }

    private void loadBudgets() {
        budgetList.getChildren().clear();
        String month = monthPicker.getValue();
        try {
            List<Budget> budgets = financeService.getBudgetsForMonth(month);
            List<Category> categories = financeService.getAllCategories();
            Map<String, String> colorMap = categories.stream()
                .collect(Collectors.toMap(Category::getName, Category::getColor));

            for (Budget b : budgets) {
                double spent = financeService.getSpentForCategory(b.getCategoryName(), month);
                double limit = b.getLimitAmount();
                double progress = limit > 0 ? Math.min(spent / limit, 1.0) : 0;
                boolean over = spent > limit;
                String color = colorMap.getOrDefault(b.getCategoryName(), "#607D8B");

                VBox card = buildBudgetCard(b, spent, limit, progress, over, color);
                budgetList.getChildren().add(card);
            }

            if (budgets.isEmpty()) {
                Label empty = new Label("Ingen budsjetter satt for denne måneden.");
                empty.getStyleClass().add("empty-label");
                budgetList.getChildren().add(empty);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox buildBudgetCard(Budget b, double spent, double limit, double progress,
                                  boolean over, String color) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        // Color dot
        javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(6);
        dot.setStyle("-fx-fill: " + color + ";");

        Label categoryLabel = new Label(b.getCategoryName());
        categoryLabel.getStyleClass().add("budget-category-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = new Label(
            CurrencyFormatter.format(spent) + " / " + CurrencyFormatter.format(limit));
        amountLabel.getStyleClass().add(over ? "budget-amount-over" : "budget-amount");

        Button editBtn = new Button("Endre");
        editBtn.getStyleClass().add("btn-secondary");
        editBtn.setOnAction(e -> editBudget(b));

        header.getChildren().addAll(dot, categoryLabel, spacer, amountLabel, editBtn);

        ProgressBar bar = new ProgressBar(progress);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().add(over ? "progress-bar-over" : "progress-bar-normal");

        Label percentLabel = new Label(String.format("%.0f%% brukt", progress * 100));
        percentLabel.getStyleClass().add(over ? "budget-amount-over" : "budget-percent");

        card.getChildren().addAll(header, bar, percentLabel);
        return card;
    }

    private void editBudget(Budget b) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(b.getLimitAmount()));
        dialog.setTitle("Endre budsjett");
        dialog.setHeaderText("Sett ny grense for " + b.getCategoryName());
        dialog.setContentText("Beløp (kr):");
        dialog.showAndWait().ifPresent(val -> {
            try {
                double newLimit = Double.parseDouble(val.replace(",", "."));
                b.setLimitAmount(newLimit);
                financeService.saveBudget(b);
                loadBudgets();
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void handleAddBudget() {
        try {
            List<Category> categories = financeService.getAllCategories();
            List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());

            ChoiceDialog<String> catDialog = new ChoiceDialog<>(names.get(0), names);
            catDialog.setTitle("Ny budsjettpost");
            catDialog.setHeaderText("Velg kategori");
            catDialog.setContentText("Kategori:");
            catDialog.showAndWait().ifPresent(catName -> {
                TextInputDialog amountDialog = new TextInputDialog("2000");
                amountDialog.setTitle("Ny budsjettpost");
                amountDialog.setHeaderText("Budsjettgrense for " + catName);
                amountDialog.setContentText("Beløp (kr):");
                amountDialog.showAndWait().ifPresent(val -> {
                    try {
                        double limit = Double.parseDouble(val.replace(",", "."));
                        Category cat = financeService.getAllCategories().stream()
                            .filter(c -> c.getName().equals(catName)).findFirst().orElse(null);
                        if (cat != null) {
                            Budget budget = new Budget(0, cat.getId(), monthPicker.getValue(), limit);
                            budget.setCategoryName(catName);
                            financeService.saveBudget(budget);
                            loadBudgets();
                        }
                    } catch (NumberFormatException | SQLException e) {
                        e.printStackTrace();
                    }
                });
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
