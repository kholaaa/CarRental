package com.example.carrental;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.*;

public class forget_passwordController {

    @FXML private TextField usernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private AnchorPane rootPane;
    @FXML private Button backButton;           // ← add in FXML

    @FXML
    public void initialize() {
        setDarkBackground(rootPane, "signup_bg.jpg");
    }

    private void setDarkBackground(AnchorPane rootPane, String imageName) {
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/com/example/carrental/pics/" + imageName));
            BackgroundImage backgroundImage = new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, true, true)
            );
            rootPane.setBackground(new Background(backgroundImage));
        } catch (Exception e) {
            rootPane.setStyle("-fx-background-color: #222222;");
        }
    }

    @FXML
    private void handleResetPassword() {
        String username = usernameField.getText().trim();
        String newPass = newPasswordField.getText().trim();

        if (username.isEmpty() || newPass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please fill in both fields!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET password = ? WHERE username = ?");
            stmt.setString(1, newPass);
            stmt.setString(2, username);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Password reset successfully! Redirecting to login...");

                // Auto redirect to login after 1.8 seconds
                PauseTransition pause = new PauseTransition(Duration.seconds(1.8));
                pause.setOnFinished(e -> goToLoginPage());
                pause.play();
            } else {
                showAlert(Alert.AlertType.ERROR, "Username not found!");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        goToLoginPage();   // or goToDashboard() if you prefer
    }

    private void goToLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Cannot load login page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" :
                type == Alert.AlertType.WARNING ? "Warning" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}