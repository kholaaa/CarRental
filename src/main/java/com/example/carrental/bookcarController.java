package com.example.carrental;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import java.sql.*;
import java.util.HashMap;

public class bookcarController {

    @FXML private TextField customerIdField;
    @FXML private ComboBox<String> carNameComboBox;
    @FXML private TextField entryDateField;
    @FXML private TextField returnDateField;

    @FXML private Button submitButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;
    @FXML private Button chalanButton;

    private HashMap<String, Integer> carNameToId = new HashMap<>();

    @FXML
    public void initialize() {
        loadAvailableCarNames();
        clearButton.setOnAction(this::handleClear);
        backButton.setOnAction(this::handleBack);
        submitButton.setOnAction(this::handleSubmit);
        chalanButton.setOnAction(this::handleChalan);
    }

    private void loadAvailableCarNames() {
        carNameComboBox.getItems().clear();
        carNameToId.clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT carID, carType FROM cars WHERE Availability = 'yes'");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("carType");
                int id = rs.getInt("carID");
                carNameComboBox.getItems().add(name);
                carNameToId.put(name, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSubmit(ActionEvent event) {
        String customerId = customerIdField.getText();
        String carName = carNameComboBox.getValue();
        String entryDate = entryDateField.getText();
        String returnDate = returnDateField.getText();

        if (customerId.isEmpty() || carName == null || entryDate.isEmpty() || returnDate.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields required.");
            return;
        }

        Integer carId = carNameToId.get(carName);
        if (carId == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid car selected.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String insertSQL = "INSERT INTO bookcar (customerID, carID, entrydate, returndate) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insertSQL);
            ps.setInt(1, Integer.parseInt(customerId));
            ps.setInt(2, carId);
            ps.setDate(3, Date.valueOf(entryDate));
            ps.setDate(4, Date.valueOf(returnDate));
            ps.executeUpdate();

            String updateSQL = "UPDATE cars SET availability = 'No' WHERE carID = ?";
            ps = conn.prepareStatement(updateSQL);
            ps.setInt(1, carId);
            ps.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Car booked successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void handleClear(ActionEvent event) {
        customerIdField.clear();
        carNameComboBox.setValue(null);
        entryDateField.clear();
        returnDateField.clear();
    }

    private void handleBack(ActionEvent event) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    private void handleChalan(ActionEvent event) {
        String cidText = customerIdField.getText();
        String carName = carNameComboBox.getValue();
        String entry = entryDateField.getText();
        String ret = returnDateField.getText();

        if (cidText.isEmpty() || carName == null || entry.isEmpty() || ret.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields required.");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT price_per_day FROM cars WHERE carType = ?")) {
            stmt.setString(1, carName);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Price info not found.");
                return;
            }

            int pricePerDay = rs.getInt("price_per_day");
            java.sql.Date entryDate = java.sql.Date.valueOf(entry);
            java.sql.Date returnDate = java.sql.Date.valueOf(ret);

            long diffDays = (returnDate.getTime() - entryDate.getTime()) / (1000 * 60 * 60 * 24) + 1;
            long totalRent = diffDays * pricePerDay;

            String message = "=== Car Rental Chalan ===\n\n"
                    + "Customer ID: " + cidText + "\n"
                    + "Car Name: " + carName + "\n"
                    + "Entry Date: " + entry + "\n"
                    + "Return Date: " + ret + "\n"
                    + "Price per Day: Rs. " + pricePerDay + "\n"
                    + "Total Days: " + diffDays + "\n"
                    + "------------------------\n"
                    + "Total Rent: Rs. " + totalRent + "\n\nThank you!";

            showAlert(Alert.AlertType.INFORMATION, "Rental Chalan", message);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}