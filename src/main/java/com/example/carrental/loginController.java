package com.example.carrental;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class loginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private AnchorPane rootPane;

    @FXML
    public void initialize() {
        loadBackgroundImage();
    }

    private void loadBackgroundImage() {
        String imagePath = "/com/example/carrental/pics/llg.png";

        try (InputStream stream = getClass().getResourceAsStream(imagePath)) {
            if (stream == null) {
                System.err.println("Warning: Background image not found at " + imagePath + " — using dark fallback");
                rootPane.setStyle("-fx-background-color: #111111;");
                return;
            }

            Image bgImage = new Image(stream);
            BackgroundImage backgroundImage = new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    BackgroundSize.DEFAULT
            );
            rootPane.setBackground(new Background(backgroundImage));

        } catch (Exception e) {
            e.printStackTrace();
            rootPane.setStyle("-fx-background-color: #111111;");
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "Please enter both username and password."));
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT id, password, role FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");

                if (storedPassword.equals(password)) {
                    int userId = rs.getInt("id");
                    String role = rs.getString("role");

                    Session.setUser(userId, role);

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                    Parent loading2Root = FXMLLoader.load(getClass().getResource("/com/example/carrental/loading2.fxml"));
                    Scene loading2Scene = new Scene(loading2Root);
                    stage.setScene(loading2Scene);

                    PauseTransition pause = new PauseTransition(Duration.seconds(2));
                    pause.setOnFinished(e -> {
                        Platform.runLater(() -> {
                            try {
                                Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/com/example/carrental/Dashboard.fxml"));
                                Scene dashboardScene = new Scene(dashboardRoot, 1280, 720);
                                stage.setTitle("Car Rental System - Dashboard");
                                stage.setScene(dashboardScene);
                                stage.centerOnScreen();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                showAlert(Alert.AlertType.ERROR, "Failed to load dashboard.");
                            }
                        });
                    });
                    pause.play();
                } else {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Incorrect password."));
                }
            } else {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Username not found."));
            }
        } catch (SQLException e) {
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage()));
            e.printStackTrace();
        } catch (IOException e) {
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Failed to load next screen."));
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent signUpRoot = FXMLLoader.load(getClass().getResource("/com/example/carrental/SignUp.fxml"));
        Scene signUpScene = new Scene(signUpRoot);
        stage.setScene(signUpScene);
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent forgotRoot = FXMLLoader.load(getClass().getResource("/com/example/carrental/forget_password.fxml"));
        Scene forgotScene = new Scene(forgotRoot);
        stage.setScene(forgotScene);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();  // ← Non-blocking show() instead of showAndWait()
    }
}