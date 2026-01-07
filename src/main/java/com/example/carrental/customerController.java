package com.example.carrental;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class customerController {

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField numberField;
    @FXML private Button backButton;         // Add this!
    @FXML private ImageView backgroundImage; // Add this for background

    @FXML
    public void initialize() {
        // Load background image
        try {
            Image img = new Image(getClass().getResourceAsStream("pics/customer_bg.jpg")); // Change to your image
            if (img.isError()) throw new Exception();
            backgroundImage.setImage(img);
        } catch (Exception e) {
            System.err.println("Customer background not found - using fallback");
            backgroundImage.setStyle("-fx-background-color: #16213e;");
        }
    }

    @FXML
    private void handleSave() {
        try {
            String idText = idField.getText().trim();
            String name = nameField.getText().trim();
            String number = numberField.getText().trim();

            if (idText.isEmpty() || name.isEmpty() || number.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Please fill all fields.");
                return;
            }

            int id = Integer.parseInt(idText);

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO customer (customerID, customername, customernumber) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, id);
                stmt.setString(2, name);
                stmt.setString(3, number);
                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Customer added successfully!");
                    handleClear();
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Customer ID must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
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
        // Fixed: Use the injected backButton
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Customer Details");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}