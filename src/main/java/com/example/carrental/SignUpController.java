package com.example.carrental;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class SignUpController {

    @FXML private ImageView backgroundImage;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField numberField;
    @FXML private CheckBox showPasswordCheck;

    private TextField visiblePasswordField = new TextField();

    @FXML
    public void initialize() {
        backgroundImage.setImage(new Image("file:C:\\CarRentalImages\\signup.jpg"));

        visiblePasswordField.managedProperty().bind(showPasswordCheck.selectedProperty());
        visiblePasswordField.visibleProperty().bind(showPasswordCheck.selectedProperty());
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        passwordField.managedProperty().bind(showPasswordCheck.selectedProperty().not());
        passwordField.visibleProperty().bind(showPasswordCheck.selectedProperty().not());
    }

    @FXML
    private void handleSignup() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String number = numberField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || number.isEmpty()) {
            showAlert("Error", "All fields required!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO login (name, email, password, PhoneNumber) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, number);
            stmt.executeUpdate();
            showAlert("Success", "Registration successful!");
            openDashboard();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/carrental/login.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/carrental/dashboard.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}