package com.example.carrental;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignUpController {

    @FXML private TextField usernameField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneField;
    @FXML private AnchorPane rootPane;
    @FXML private Button backToLoginButton;  // NEW: Back button

    @FXML
    public void initialize() {
        // Load background safely
        String imageName = "signup_bg.jpg";  // ← Change to your actual image name
        setDarkBackground(rootPane, imageName);

        // Always show the back button
        backToLoginButton.setVisible(true);
    }

    private void setDarkBackground(AnchorPane rootPane, String imageName) {
        String fullPath = "/com/example/carrental/pics/" + imageName;
        try (InputStream stream = getClass().getResourceAsStream(fullPath)) {
            if (stream == null) {
                System.err.println("Signup background not found: " + fullPath + " — using dark fallback");
                rootPane.setStyle("-fx-background-color: #111111;");
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
            rootPane.setStyle("-fx-background-color: #111111;");
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        String username = usernameField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String phone = phoneField.getText().trim();

        // Basic validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Username, Email and Password are required!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, name, email, password, PhoneNumber, role) " +
                    "VALUES (?, ?, ?, ?, ?, 'customer')";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, name.isEmpty() ? "" : name);
            stmt.setString(3, email);
            stmt.setString(4, password);  // ← Hash in production!
            stmt.setString(5, phone.isEmpty() ? null : phone);

            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Registration successful! Redirecting to login...");

            // Auto-redirect to login page
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/example/carrental/login.fxml"));
            Scene loginScene = new Scene(loginRoot);
            stage.setScene(loginScene);
            stage.centerOnScreen();

        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("duplicate entry") && msg.contains("username")) {
                showAlert(Alert.AlertType.ERROR, "This username is already taken.");
            } else if (msg.contains("duplicate entry") && msg.contains("email")) {
                showAlert(Alert.AlertType.ERROR, "This email is already registered.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
            }
            e.printStackTrace();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load login page.");
            e.printStackTrace();
        }
    }

    // NEW: Back to Login button (always visible)
    @FXML
    private void handleBackToLogin(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/example/carrental/login.fxml"));
        Scene loginScene = new Scene(loginRoot);
        stage.setScene(loginScene);
        stage.centerOnScreen();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Car Rental System");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}