package com.example.carrental;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class bookcarController {

    @FXML private TextField carIdField;
    @FXML private DatePicker entryDatePicker;
    @FXML private DatePicker returnDatePicker;
    @FXML private AnchorPane rootPane;

    @FXML
    public void initialize() {
        loadBackgroundImage();
    }

    private void loadBackgroundImage() {
        // Change to your actual image name (must be in src/main/resources/com/example/carrental/pics/)
        String imagePath = "/com/example/carrental/pics/llg.png";  // ← dark elegant car background recommended

        try (InputStream stream = getClass().getResourceAsStream(imagePath)) {
            if (stream == null) {
                System.err.println("Background image not found: " + imagePath + " → using dark fallback");
                rootPane.setStyle("-fx-background-color: #0f171e;");
                return;
            }

            Image bgImage = new Image(stream);
            BackgroundImage backgroundImage = new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
            );

            rootPane.setBackground(new Background(backgroundImage));

        } catch (Exception e) {
            e.printStackTrace();
            rootPane.setStyle("-fx-background-color: #0f171e;");
        }
    }

    @FXML
    private void handleBookCar(ActionEvent event) {
        try {
            int carId = Integer.parseInt(carIdField.getText().trim());

            LocalDate entryDate = entryDatePicker.getValue();
            LocalDate returnDate = returnDatePicker.getValue();

            if (entryDate == null || returnDate == null) {
                showAlert(Alert.AlertType.ERROR, "Please select both start and return dates.");
                return;
            }

            if (returnDate.isBefore(entryDate) || returnDate.isEqual(entryDate)) {
                showAlert(Alert.AlertType.ERROR, "Return date must be after start date.");
                return;
            }

            long days = ChronoUnit.DAYS.between(entryDate, returnDate);
            if (days <= 0) {
                showAlert(Alert.AlertType.ERROR, "Minimum rental period is 1 day.");
                return;
            }

            int customerId = Session.getUserId();
            if (customerId == -1) {
                showAlert(Alert.AlertType.ERROR, "You must be logged in to book a car.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                // 1. Check car availability
                String carQuery = "SELECT price_per_day FROM cars WHERE carID = ? AND Availability = 'Yes'";
                PreparedStatement carStmt = conn.prepareStatement(carQuery);
                carStmt.setInt(1, carId);
                ResultSet carRs = carStmt.executeQuery();

                if (!carRs.next()) {
                    showAlert(Alert.AlertType.ERROR, "Car not found or currently unavailable.");
                    return;
                }

                double pricePerDay = carRs.getDouble("price_per_day");
                double totalCost = days * pricePerDay;

                // 2. Check for overlapping bookings
                String overlapQuery =
                        "SELECT COUNT(*) FROM bookcar " +
                                "WHERE carID = ? AND " +
                                "(entrydate <= ? AND returndate >= ?)";
                PreparedStatement overlapStmt = conn.prepareStatement(overlapQuery);
                overlapStmt.setInt(1, carId);
                overlapStmt.setDate(2, Date.valueOf(returnDate));
                overlapStmt.setDate(3, Date.valueOf(entryDate));
                ResultSet overlapRs = overlapStmt.executeQuery();
                overlapRs.next();

                if (overlapRs.getInt(1) > 0) {
                    showAlert(Alert.AlertType.ERROR, "This car is already booked for the selected dates.");
                    return;
                }

                // 3. Create booking
                String insertQuery =
                        "INSERT INTO bookcar (carID, customerID, entrydate, returndate, total_cost) " +
                                "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, carId);
                insertStmt.setInt(2, customerId);
                insertStmt.setDate(3, Date.valueOf(entryDate));
                insertStmt.setDate(4, Date.valueOf(returnDate));
                insertStmt.setDouble(5, totalCost);
                insertStmt.executeUpdate();

                // 4. Mark car unavailable
                String updateCar = "UPDATE cars SET Availability = 'No' WHERE carID = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateCar);
                updateStmt.setInt(1, carId);
                updateStmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION,
                        "Booking successful!\n" +
                                "Rental Days: " + days + "\n" +
                                "Total Cost: " + totalCost + " PKR");

                // Clear form
                carIdField.clear();
                entryDatePicker.setValue(null);
                returnDatePicker.setValue(null);

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Car ID must be a valid number.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/com/example/carrental/Dashboard.fxml"));
            Scene scene = new Scene(dashboardRoot);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load Dashboard.\n" + e.getMessage());
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