package com.financetracker.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;

    @FXML private Button btnDashboard;
    @FXML private Button btnTransactions;
    @FXML private Button btnBudget;
    @FXML private Button btnStatistics;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        navButtons = List.of(btnDashboard, btnTransactions, btnBudget, btnStatistics);
        showDashboard();
    }

    @FXML
    public void showDashboard() {
        loadView("dashboard.fxml");
        setActive(btnDashboard);
    }

    @FXML
    public void showTransactions() {
        loadView("transactions.fxml");
        setActive(btnTransactions);
    }

    @FXML
    public void showBudget() {
        loadView("budget.fxml");
        setActive(btnBudget);
    }

    @FXML
    public void showStatistics() {
        loadView("statistics.fxml");
        setActive(btnStatistics);
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/financetracker/fxml/" + fxmlFile));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActive(Button active) {
        for (Button b : navButtons) {
            b.getStyleClass().remove("nav-active");
        }
        active.getStyleClass().add("nav-active");
    }
}
