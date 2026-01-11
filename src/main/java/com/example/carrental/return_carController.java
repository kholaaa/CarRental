package com.example.carrental;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class return_carController {

    @FXML private TextField carIdField;
    @FXML private TextField fuelLevelField;
    @FXML private TextField conditionField;
    @FXML private AnchorPane rootPane;

    @FXML
    private void handleReturnCar(ActionEvent event) {

        try {
            int carId = Integer.parseInt(carIdField.getText().trim());
            int fuelLevel = Integer.parseInt(fuelLevelField.getText().trim());
            String condition = conditionField.getText().trim();

            if (fuelLevel < 0 || fuelLevel > 100) {
                showAlert(Alert.AlertType.ERROR, "Fuel level must be between 0 and 100.");
                return;
            }

            if (condition.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Please describe the car condition.");
                return;
            }

            int customerId = Session.getUserId();
            if (customerId == -1) {
                showAlert(Alert.AlertType.ERROR, "You must be logged in to return a car.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {

                // 1. Check ACTIVE booking (important)
                String checkQuery = """
                    SELECT bookcarID 
                    FROM bookcar 
                    WHERE carID = ? 
                    AND customerID = ?
                    AND returndate >= CURDATE()
                """;

                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, carId);
                checkStmt.setInt(2, customerId);

                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    showAlert(Alert.AlertType.ERROR,
                            "You do not have an active booking for this car.");
                    return;
                }

                // 2. Insert into returncar (KEEP booking history)
                String insertReturn = """
                    INSERT INTO returncar 
                    (carID, customerID, returndate, fuellevel, carcondition)
                    VALUES (?, ?, CURDATE(), ?, ?)
                """;

                PreparedStatement insertStmt = conn.prepareStatement(insertReturn);
                insertStmt.setInt(1, carId);
                insertStmt.setInt(2, customerId);
                insertStmt.setInt(3, fuelLevel);
                insertStmt.setString(4, condition);
                insertStmt.executeUpdate();

                // 3. Mark car as available again
                String updateCar = "UPDATE cars SET Availability = 'Yes' WHERE carID = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateCar);
                updateStmt.setInt(1, carId);
                updateStmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION,
                        "Car returned successfully!\nIt is now available for booking.");

                // Clear fields
                carIdField.clear();
                fuelLevelField.clear();
                conditionField.clear();

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR,
                        "Database error: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Car ID and Fuel Level must be valid numbers.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage =
                    (Stage) ((Node) event.getSource()).getScene().getWindow();

            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/carrental/Dashboard.fxml"));

            stage.setScene(new Scene(root));
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Unable to load Dashboard.");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Car Rental System");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
