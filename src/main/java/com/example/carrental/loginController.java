package com.example.carrental;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

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
        // Load background image
        try {
            backgroundImage.setImage(new Image(getClass().getResourceAsStream("/com/example/carrental/pics/car2.png")));
        } catch (Exception e) {
            System.out.println("Login background image not found in resources.");
        }

        // Password show/hide binding
        visiblePasswordField.managedProperty().bind(showPassword.selectedProperty());
        visiblePasswordField.visibleProperty().bind(showPassword.selectedProperty());
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        passwordField.managedProperty().bind(showPassword.selectedProperty().not());
        passwordField.visibleProperty().bind(showPassword.selectedProperty().not());
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
                // Close login window
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.close();
                // Open first loading screen
                openLoadingScreen();
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
        // Open signup in same stage
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/signup.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Sign Up - Car Rental System");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgot() {
        openWindow("/com/example/carrental/forget_password.fxml", "Forgot Password");
    }

    private void openLoadingScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/loading.fxml"));
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
            // Close current window
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Could not open " + title);
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
