package com.example.carrental;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class add_carController {

    @FXML private TextField carIdField;
    @FXML private TextField modelField;
    @FXML private TextField typeField;      // Changed from brandField → better naming
    @FXML private TextField colourField;    // Added missing colour field
    @FXML private TextField priceField;
    @FXML private AnchorPane rootPane;

    @FXML
    public void initialize() {
        loadBackgroundImage();
    }

    private void loadBackgroundImage() {
        // Change this to your actual image name (place it in src/main/resources/com/example/carrental/pics/)
        String imagePath = "/com/example/carrental/pics/llg.png";  // ← recommended dark car-themed image

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
    private void handleAddCar(ActionEvent event) {
        String carIdStr = carIdField.getText().trim();
        String model = modelField.getText().trim();
        String type = typeField.getText().trim();
        String colour = colourField.getText().trim();
        String priceStr = priceField.getText().trim();

        if (carIdStr.isEmpty() || model.isEmpty() || type.isEmpty() || colour.isEmpty() || priceStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "All fields are required!");
            return;
        }

        try {
            int carId = Integer.parseInt(carIdStr);
            double price = Double.parseDouble(priceStr);

            if (price <= 0) {
                showAlert(Alert.AlertType.ERROR, "Price must be greater than 0.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String query = "INSERT INTO cars (carID, carmodel, cartype, colour, price_per_day, Availability) " +
                        "VALUES (?, ?, ?, ?, ?, 'Yes')";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, carId);
                stmt.setString(2, model);
                stmt.setString(3, type);
                stmt.setString(4, colour);
                stmt.setDouble(5, price);

                stmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Car added successfully!");
                clearFields();

            } catch (SQLException e) {
                if (e.getMessage().toLowerCase().contains("duplicate entry")) {
                    showAlert(Alert.AlertType.ERROR, "Car ID already exists!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
                }
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Car ID and Price must be valid numbers.");
        }
    }

    private void clearFields() {
        carIdField.clear();
        modelField.clear();
        typeField.clear();
        colourField.clear();
        priceField.clear();
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