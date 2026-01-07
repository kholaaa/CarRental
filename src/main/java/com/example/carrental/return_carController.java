package com.example.carrental;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;

public class ReturnCar {

    @FXML private TextField carIdField;
    @FXML private TextField customerIdField;
    @FXML private TextField returnDateField;
    @FXML private TextField fuelLevelField;
    @FXML private TextField conditionField;

    @FXML
    private void submitReturn() {
        String carId = carIdField.getText();
        String customerId = customerIdField.getText();
        String returnDate = returnDateField.getText();
        String fuelLevel = fuelLevelField.getText();
        String condition = conditionField.getText();

        if (carId.isEmpty() || customerId.isEmpty() || returnDate.isEmpty() || fuelLevel.isEmpty() || condition.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill all fields");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String insertSQL = "INSERT INTO returncar (fuellevel, carcondition, carID, customerID, returndate) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insertSQL);
            ps.setInt(1, Integer.parseInt(fuelLevel));
            ps.setString(2, condition);
            ps.setInt(3, Integer.parseInt(carId));
            ps.setInt(4, Integer.parseInt(customerId));
            ps.setDate(5, Date.valueOf(returnDate));
            ps.executeUpdate();

            String updateSQL = "UPDATE cars SET availability='Yes' WHERE carID=?";
            ps = conn.prepareStatement(updateSQL);
            ps.setInt(1, Integer.parseInt(carId));
            ps.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Car returned successfully!");
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    private void clearFields() {
        carIdField.clear();
        customerIdField.clear();
        returnDateField.clear();
        fuelLevelField.clear();
        conditionField.clear();
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) carIdField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}