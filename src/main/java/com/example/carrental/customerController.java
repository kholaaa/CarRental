package com.example.carrental;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class customerController {

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField numberField;

    @FXML
    private void handleSave() {
        try {
            int id = Integer.parseInt(idField.getText());
            String name = nameField.getText();
            String number = numberField.getText();

            if (name.isEmpty() || number.isEmpty()) {
                showAlert("All fields required");
                return;
            }

            Connection conn = DBConnection.getConnection();
            String sql = "INSERT INTO customers (customerID, name, phone) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setString(2, name);
            stmt.setString(3, number);
            stmt.executeUpdate();
            showAlert("Customer added");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error");
        }
    }

    @FXML
    private void handleClear() {
        idField.clear();
        nameField.clear();
        numberField.clear();
    }

    @FXML
    private void handleBack() {
        idField.getScene().getWindow().hide();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }
}