package com.example.carrental;


import javafx.fxml.FXML;
import javafx.scene.control.*;
        import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class forget_passwordController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    private void handleSubmit() {

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()
                || newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill all fields.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Passwords do not match.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            String check = "SELECT * FROM login WHERE name=? AND email=? AND PhoneNumber=?";
            PreparedStatement ps = conn.prepareStatement(check);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String update = "UPDATE login SET password=? WHERE name=? AND email=? AND PhoneNumber=?";
                PreparedStatement up = conn.prepareStatement(update);
                up.setString(1, newPass);
                up.setString(2, name);
                up.setString(3, email);
                up.setString(4, phone);

                if (up.executeUpdate() > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Password updated successfully!");
                    closeWindow();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "User not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleBack() {
        closeWindow();
        // You can open LoginFX here
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
