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
import javafx.scene.layout.*;
import javafx.stage.Stage;

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
    @FXML private Button backToLoginButton;

    @FXML
    public void initialize() {
        loadBackgroundImage();
        backToLoginButton.setVisible(true);
    }

    private void loadBackgroundImage() {
        String path = "/com/example/carrental/pics/signup_bg.jpg";

        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            System.out.println("❌ Image not found: " + path);
            rootPane.setStyle("-fx-background-color: #111;");
            return;
        }

        Image image = new Image(stream);
        BackgroundImage bg = new BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(
                        BackgroundSize.AUTO,
                        BackgroundSize.AUTO,
                        true,
                        true,
                        true,
                        true
                )
        );

        rootPane.setBackground(new Background(bg));
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Username, Email and Password are required!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, name, email, password, PhoneNumber, role) " +
                    "VALUES (?, ?, ?, ?, ?, 'customer')";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, nameField.getText());
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.setString(5, phoneField.getText());

            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Registration successful!");

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/carrental/login.fxml"));
            stage.setScene(new Scene(root));
            stage.centerOnScreen();

        } catch (SQLException | java.io.IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error occurred!");
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) throws java.io.IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/carrental/login.fxml"));
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle("Car Rental System");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
