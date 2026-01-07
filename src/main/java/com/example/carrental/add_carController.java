package com.example.carrental;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class add_carController {

    @FXML private TextField carIdField;
    @FXML private TextField typeField;
    @FXML private TextField colorField;
    @FXML private TextField modelField;
    @FXML private TextField rentField;

    @FXML
    private void handleSave() {
        try {
            int carId = Integer.parseInt(carIdField.getText());
            int rent = Integer.parseInt(rentField.getText());

            String type = typeField.getText();
            String color = colorField.getText();
            String model = modelField.getText();

            if (type.isEmpty() || color.isEmpty() || model.isEmpty()) {
                showAlert("All fields are required");
                return;
            }

            Connection conn = DBConnection.getConnection();
            String sql = "INSERT INTO cars (carid, carmodel, cartype, colour, price_per_day) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, carId);
            stmt.setString(2, model);
            stmt.setString(3, type);
            stmt.setString(4, color);
            stmt.setInt(5, rent);

            stmt.executeUpdate();
            showAlert("Car added successfully");

            stmt.close();
            conn.close();

        } catch (NumberFormatException e) {
            showAlert("Car ID and Rent must be numbers");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database error");
        }
    }

    @FXML
    private void handleClear() {
        carIdField.clear();
        typeField.clear();
        colorField.clear();
        modelField.clear();
        rentField.clear();
    }

    @FXML
    private void handleBack() {
        System.out.println("Back button clicked");
        // load dashboard scene here
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }
}
