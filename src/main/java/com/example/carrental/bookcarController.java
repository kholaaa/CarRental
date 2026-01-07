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
            if (carNameComboBox.getItems().isEmpty()) {
                carNameComboBox.getItems().add("No cars available");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading cars: " + e.getMessage());
        }
    }

    private void handleClear(ActionEvent e) {
        customerIdField.clear();
        entryDateField.clear();
        returnDateField.clear();
        if (!carNameComboBox.getItems().isEmpty()) carNameComboBox.getSelectionModel().selectFirst();
    }

    private void handleBack(ActionEvent e) {
        // Close current window and open dashboard
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
        new dashboard(); // convert dashboard to JavaFX
    }

    private void handleSubmit(ActionEvent e) {
        String cidText = customerIdField.getText().trim();
        String carName = carNameComboBox.getSelectionModel().getSelectedItem();
        String entry = entryDateField.getText().trim();
        String ret = returnDateField.getText().trim();

        if (cidText.isEmpty() || carName == null || entry.isEmpty() || ret.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        Integer carIdObj = carNameToId.get(carName);
        if (carIdObj == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid car selected.");
            return;
        }
        int carID = carIdObj;

        int customerID;
        try { customerID = Integer.parseInt(cidText); }
        catch (NumberFormatException ex) { showAlert(Alert.AlertType.ERROR, "Invalid Input", "Customer ID must be a number."); return; }

        java.sql.Date newEntryDate, newReturnDate;
        try {
            newEntryDate = java.sql.Date.valueOf(entry);
            newReturnDate = java.sql.Date.valueOf(ret);
            if (!newReturnDate.after(newEntryDate)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Date", "Return date must be after entry date.");
                return;
            }
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid date format. Use YYYY-MM-DD.");
            return;
        }

        // Check overlap
        boolean hasOverlap = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT entrydate, returndate FROM bookcar WHERE carID = ?")) {
            stmt.setInt(1, carID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                java.sql.Date existingEntry = rs.getDate("entrydate");
                java.sql.Date existingReturn = rs.getDate("returndate");
                if (newEntryDate.compareTo(existingReturn) <= 0 && newReturnDate.compareTo(existingEntry) >= 0) {
                    hasOverlap = true;
                    break;
                }
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error checking availability: " + ex.getMessage());
            return;
        }

        if (hasOverlap) {
            showAlert(Alert.AlertType.WARNING, "Booking Conflict", "This car is already booked for the selected dates.");
            return;
        }

        // Insert booking
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO bookcar (carID, customerID, entrydate, returndate) VALUES (?, ?, ?, ?)")) {

            stmt.setInt(1, carID);
            stmt.setInt(2, customerID);
            stmt.setDate(3, newEntryDate);
            stmt.setDate(4, newReturnDate);

            int result = stmt.executeUpdate();
            if (result > 0) {
                PreparedStatement update = conn.prepareStatement("UPDATE cars SET Availability = 'No' WHERE carID = ?");
                update.setInt(1, carID);
                update.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Booking Successful!");
                handleClear(null);
                loadAvailableCarNames();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Booking Failed!");
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error saving booking: " + ex.getMessage());
        }
    }

    private void handleChalan(ActionEvent e) {
        String cidText = customerIdField.getText().trim();
        String carName = carNameComboBox.getSelectionModel().getSelectedItem();
        String entry = entryDateField.getText().trim();
        String ret = returnDateField.getText().trim();

        if (cidText.isEmpty() || carName == null || entry.isEmpty() || ret.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Fill all fields before generating chalan.");
            return;
        }

        Integer carID = carNameToId.get(carName);
        if (carID == null) { showAlert(Alert.AlertType.ERROR, "Error", "Invalid car."); return; }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT price_per_day FROM cars WHERE carID = ?")) {

            stmt.setInt(1, carID);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) { showAlert(Alert.AlertType.ERROR, "Error", "Price info not found."); return; }

            int pricePerDay = rs.getInt("price_per_day");
            java.sql.Date entryDate = java.sql.Date.valueOf(entry);
            java.sql.Date returnDate = java.sql.Date.valueOf(ret);

            long diffDays = (returnDate.getTime() - entryDate.getTime()) / (1000*60*60*24) + 1;
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
