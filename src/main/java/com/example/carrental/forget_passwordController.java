package com.example.carrental;

import javafx.animation.PauseTransition;
import com.example.carrental.chat.FloatingChatButton;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class forget_passwordController {

    @FXML private TextField usernameField;
    @FXML private TextField otpField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private VBox step1Box;
    @FXML private VBox step2Box;
    @FXML private VBox step3Box;
    @FXML private Label stepLabel;
    @FXML private Label otpMessage;
    @FXML private Button sendOtpBtn;
    @FXML private Button verifyOtpBtn;
    @FXML private Button resetBtn;
    @FXML private AnchorPane rootPane;

    private String verifiedUsername;

    @FXML
    public void initialize() {
        setDarkBackground(rootPane, "signup_bg.jpg");
        FloatingChatButton.install(rootPane);
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
    private void handleSendOtp() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please enter your username.");
            return;
        }

        long waitSeconds = OtpService.secondsBeforeResendAllowed(username);
        if (waitSeconds > 0) {
            showAlert(Alert.AlertType.WARNING,
                    "Please wait " + waitSeconds + " seconds before requesting a new code.");
            return;
        }

        String email = lookupEmail(username);
        if (email == null) {
            step1Box.setVisible(false);
            step1Box.setManaged(false);
            step2Box.setVisible(true);
            step2Box.setManaged(true);
            stepLabel.setText("Step 2 of 3");
            otpMessage.setText("If that account exists, a verification code was sent to the email on file.");
            return;
        }

        String code = OtpService.issueOtp(username);
        boolean emailed;
        try {
            emailed = MailService.sendOtp(email, code);
        } catch (Exception e) {
            e.printStackTrace();
            emailed = false;
        }

        step1Box.setVisible(false);
        step1Box.setManaged(false);
        step2Box.setVisible(true);
        step2Box.setManaged(true);
        stepLabel.setText("Step 2 of 3");

        if (emailed) {
            otpMessage.setText("A verification code was sent to the email on file. It expires in 10 minutes.");
        } else {
            otpMessage.setText("Development mode: the code was shown on screen because no email is configured.");
            showAlert(Alert.AlertType.INFORMATION,
                    "Development mode: your verification code is " + code
                            + "\n\nTo send real emails, fill in SMTP settings in src/main/resources/mail.properties.");
        }
    }

    @FXML
    private void handleVerifyOtp() {
        String username = usernameField.getText().trim();
        String code = otpField.getText().trim();

        if (username.isEmpty() || code.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please enter the verification code.");
            return;
        }

        OtpService.OtpVerifyResult result = OtpService.verifyOtp(username, code);
        switch (result) {
            case OK -> {
                verifiedUsername = username;
                step2Box.setVisible(false);
                step2Box.setManaged(false);
                step3Box.setVisible(true);
                step3Box.setManaged(true);
                stepLabel.setText("Step 3 of 3");
            }
            case INVALID -> showAlert(Alert.AlertType.ERROR, "Incorrect code. Please try again.");
            case EXPIRED -> showAlert(Alert.AlertType.ERROR, "This code has expired. Please request a new one.");
            case TOO_MANY_ATTEMPTS -> showAlert(Alert.AlertType.ERROR,
                    "Too many incorrect attempts. Please request a new code.");
            case NOT_ISSUED -> showAlert(Alert.AlertType.ERROR,
                    "No code was requested for this account. Please go back and request a new one.");
        }
    }

    @FXML
    private void handleResetPassword() {
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please fill in both password fields!");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.WARNING, "The passwords do not match.");
            return;
        }

        String strengthError = PasswordUtil.strengthError(newPass);
        if (strengthError != null) {
            showAlert(Alert.AlertType.WARNING, strengthError);
            return;
        }

        if (verifiedUsername == null) {
            showAlert(Alert.AlertType.ERROR, "Your session has expired. Please start over.");
            return;
        }

        String hashed = PasswordUtil.hash(newPass);

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET password = ? WHERE username = ?");
            stmt.setString(1, hashed);
            stmt.setString(2, verifiedUsername);

            int rows = stmt.executeUpdate();
            OtpService.invalidate(verifiedUsername);

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Password reset successfully! Redirecting to login...");

                PauseTransition pause = new PauseTransition(Duration.seconds(1.8));
                pause.setOnFinished(e -> goToLoginPage());
                pause.play();
            } else {
                showAlert(Alert.AlertType.ERROR, "Account not found. Please try again.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String lookupEmail(String username) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT email FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    private void handleBack() {
        goToLoginPage();
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
