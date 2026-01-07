package com.example.carrental;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class loginController {

    @FXML private ImageView backgroundImage;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPassword;

    @FXML
    public void initialize() {
        // Load background image from resources
        try {
            backgroundImage.setImage(new Image(getClass().getResourceAsStream("/com/example/carrental/images/login.jpg")));
        } catch (Exception e) {
            System.out.println("Login background image not found in resources.");
            // Optional fallback to absolute path
            // backgroundImage.setImage(new Image("file:C:\\CarRentalImages\\login.jpg"));
        }

        // Password show/hide binding
        visiblePasswordField.managedProperty().bind(showPassword.selectedProperty());
        visiblePasswordField.visibleProperty().bind(showPassword.selectedProperty());
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        passwordField.managedProperty().bind(showPassword.selectedProperty().not());
        passwordField.visibleProperty().bind(showPassword.selectedProperty().not());
    }

    @FXML
    private void handleShowPassword() {
        // Required for FXML onAction — binding already handles logic
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please enter email and password.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM login WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                showAlert(Alert.AlertType.INFORMATION, "Login Successful!");

                closeCurrentWindow();
                openLoadingScreen();  // Go to first loading screen

            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid email or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignup() {
        openWindow("/com/example/carrental/signup.fxml", "Sign Up");
    }

    @FXML
    private void handleForgot() {
        openWindow("/com/example/carrental/forget_password.fxml", "Forgot Password");
    }

    private void openLoadingScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/Loading.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setTitle("Loading...");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            closeCurrentWindow();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Could not open " + title);
        }
    }

    private void closeCurrentWindow() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}