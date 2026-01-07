package com.example.carrental;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;         // ← Already there
import javafx.scene.image.ImageView;    // ← ADD THIS LINE
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
        // Load background image
        try {
            backgroundImage.setImage(new Image(getClass().getResourceAsStream("/com/example/carrental/images/login.jpg")));
        } catch (Exception e) {
            System.out.println("Login background image not found.");
        }

        // Password visibility binding (handles show/hide automatically)
        visiblePasswordField.managedProperty().bind(showPassword.selectedProperty());
        visiblePasswordField.visibleProperty().bind(showPassword.selectedProperty());
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        passwordField.managedProperty().bind(showPassword.selectedProperty().not());
        passwordField.visibleProperty().bind(showPassword.selectedProperty().not());
    }

    @FXML
    private void handleShowPassword() {
        // This method is required for the onAction in FXML
        // The binding above already handles everything, so body can be empty
    }

    // ... rest of your code (handleLogin, handleSignup, handleForgot, helpers) remains the same ...
}