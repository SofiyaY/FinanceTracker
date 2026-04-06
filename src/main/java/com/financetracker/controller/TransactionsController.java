package com.financetracker.controller;

import com.financetracker.model.Transaction;
import com.financetracker.service.ExportService;
import com.financetracker.service.FinanceService;
import com.financetracker.util.CurrencyFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public class TransactionsController {

    @FXML private TableView<Transaction> tableView;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colName;
    @FXML private TableColumn<Transaction, String> colCategory;
    @FXML private TableColumn<Transaction, String> colAmount;

    @FXML private ComboBox<String> filterMonth;
    @FXML private ComboBox<String> filterCategory;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnExport;

    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private FinanceService financeService;
    private ExportService exportService;

    @FXML
    public void initialize() {
        try {
            financeService = new FinanceService();
            exportService = new ExportService();
            setupTable();
            setupFilters();
            loadTransactions();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate().toString()));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAmount.setCellValueFactory(c -> {
            double amount = c.getValue().getAmount();
            return new SimpleStringProperty(CurrencyFormatter.formatSigned(amount));
        });
        colAmount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(item.startsWith("-")
                        ? "-fx-text-fill: #e53935;"
                        : "-fx-text-fill: #43a047;");
                }
            }
        });
        tableView.setItems(transactions);
        tableView.setPlaceholder(new Label("Ingen transaksjoner funnet."));
    }

    private void setupFilters() throws SQLException {
        // Month filter — last 12 months
        filterMonth.getItems().add("Alle måneder");
        YearMonth now = YearMonth.now();
        for (int i = 0; i < 12; i++) {
            filterMonth.getItems().add(now.minusMonths(i).toString());
        }
        filterMonth.setValue(now.toString());

        // Category filter
        filterCategory.getItems().add("Alle kategorier");
        filterCategory.getItems().addAll(financeService.getCategoryNames());
        filterCategory.setValue("Alle kategorier");

        filterMonth.setOnAction(e -> loadTransactions());
        filterCategory.setOnAction(e -> loadTransactions());
    }

    private void loadTransactions() {
        try {
            String month = filterMonth.getValue();
            String category = filterCategory.getValue();

            List<Transaction> result;
            if ("Alle måneder".equals(month) && "Alle kategorier".equals(category)) {
                result = financeService.getAllTransactions();
            } else if ("Alle måneder".equals(month)) {
                result = financeService.getTransactionsByMonthAndCategory(null, category);
            } else if ("Alle kategorier".equals(category)) {
                result = financeService.getTransactionsByMonth(month);
            } else {
                result = financeService.getTransactionsByMonthAndCategory(month, category);
            }
            transactions.setAll(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAdd() {
        openDialog(null);
    }

    @FXML
    public void handleEdit() {
        Transaction selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Velg en transaksjon å redigere.");
            return;
        }
        openDialog(selected);
    }

    @FXML
    public void handleDelete() {
        Transaction selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Velg en transaksjon å slette.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Slett \"" + selected.getName() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Bekreft sletting");
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                financeService.deleteTransaction(selected.getId());
                loadTransactions();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Eksporter transaksjoner");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        chooser.setInitialFileName("transaksjoner.json");
        File file = chooser.showSaveDialog(tableView.getScene().getWindow());
        if (file != null) {
            try {
                exportService.exportToJson(transactions, file);
                showInfo("Eksport fullført: " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Eksport feilet: " + e.getMessage());
            }
        }
    }

    private void openDialog(Transaction transaction) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/financetracker/fxml/transaction_dialog.fxml"));
            DialogPane dialogPane = loader.load();
            TransactionDialogController dialogCtrl = loader.getController();
            dialogCtrl.setFinanceService(financeService);
            dialogCtrl.setTransaction(transaction);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(transaction == null ? "Ny transaksjon" : "Rediger transaksjon");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Transaction t = dialogCtrl.buildTransaction();
                if (t != null) {
                    if (transaction == null) {
                        financeService.addTransaction(t);
                    } else {
                        t.setId(transaction.getId());
                        financeService.updateTransaction(t);
                    }
                    loadTransactions();
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
