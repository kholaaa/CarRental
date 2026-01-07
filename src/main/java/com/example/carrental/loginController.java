package com.example.carrental;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private ImageView backgroundImage;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private CheckBox showPassword;

    @FXML
    public void initialize() {
        // Load background image
        backgroundImage.setImage(
                new Image(getClass().getResourceAsStream("/car/rental/system/login.jpg"))
        );
    }

    @FXML
    private void handleShowPassword() {
        if (showPassword.isSelected()) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            passwordField.setVisible(false);
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            visiblePasswordField.setVisible(false);
        }
    }

    @FXML
    private void handleLogin() {

        String email = emailField.getText();
        String password = showPassword.isSelected()
                ? visiblePasswordField.getText()
                : passwordField.getText();

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT * FROM login WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Login Successful!");
                alert.showAndWait();

                // Open dashboard
                new Dashboard().start(new Stage());

                // Close login window
                ((Stage) emailField.getScene().getWindow()).close();

            } else {
                new Alert(Alert.AlertType.ERROR, "Invalid email or password.").show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Database error:\n" + e.getMessage()).show();
        }
    }

    @FXML
    private void handleSignup() throws Exception {
        ((Stage) emailField.getScene().getWindow()).close();
        new SignUp().start(new Stage());
    }

    @FXML
    private void handleForgot() throws Exception {
        new ForgetPassword().start(new Stage());
    }
}
