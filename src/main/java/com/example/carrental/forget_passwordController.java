

package com.example.carrental;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.sql.*;

public class forget_passwordController {
    @FXML private TextField usernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private AnchorPane rootPane;

    @FXML
    public void initialize() {
        setDarkBackground(rootPane, "signup_bg.jpg"); // Reuse same background
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
            // Fallback to solid dark if image not found
            rootPane.setStyle("-fx-background-color: #222222;");
        }
    }

    @FXML
    private void handleResetPassword() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET password = ? WHERE username = ?");
            stmt.setString(1, newPasswordField.getText());
            stmt.setString(2, usernameField.getText());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Password reset successfully! Please login.");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Username not found.");
                alert.show();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
            alert.show();
        }
    }
}