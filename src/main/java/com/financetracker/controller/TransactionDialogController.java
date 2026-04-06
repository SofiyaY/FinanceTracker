package com.financetracker.controller;

import com.financetracker.model.Transaction;
import com.financetracker.service.FinanceService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.LocalDate;

public class TransactionDialogController {

    @FXML private DatePicker fieldDate;
    @FXML private TextField fieldName;
    @FXML private ComboBox<String> fieldCategory;
    @FXML private TextField fieldAmount;
    @FXML private TextArea fieldNote;
    @FXML private ToggleGroup typeGroup;
    @FXML private RadioButton radioIncome;
    @FXML private RadioButton radioExpense;

    private FinanceService financeService;

    public void setFinanceService(FinanceService service) throws SQLException {
        this.financeService = service;
        fieldCategory.getItems().setAll(service.getCategoryNames());
    }

    public void setTransaction(Transaction t) {
        if (t == null) {
            fieldDate.setValue(LocalDate.now());
            radioExpense.setSelected(true);
            return;
        }
        fieldDate.setValue(t.getDate());
        fieldName.setText(t.getName());
        fieldCategory.setValue(t.getCategory());
        double abs = Math.abs(t.getAmount());
        fieldAmount.setText(String.valueOf(abs));
        fieldNote.setText(t.getNote() != null ? t.getNote() : "");
        if (t.isIncome()) {
            radioIncome.setSelected(true);
        } else {
            radioExpense.setSelected(true);
        }
    }

    public Transaction buildTransaction() {
        String name = fieldName.getText().trim();
        String category = fieldCategory.getValue();
        String amountText = fieldAmount.getText().trim().replace(",", ".");
        LocalDate date = fieldDate.getValue();

        if (name.isEmpty() || category == null || amountText.isEmpty() || date == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Fyll ut alle påkrevde felt.", ButtonType.OK);
            a.setHeaderText(null);
            a.showAndWait();
            return null;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Ugyldig beløp.", ButtonType.OK);
            a.setHeaderText(null);
            a.showAndWait();
            return null;
        }

        if (radioExpense.isSelected()) amount = -Math.abs(amount);
        else amount = Math.abs(amount);

        Transaction t = new Transaction();
        t.setDate(date);
        t.setName(name);
        t.setCategory(category);
        t.setAmount(amount);
        t.setNote(fieldNote.getText().trim());
        return t;
    }
}
