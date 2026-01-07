package com.example.carrental;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class loading2Controller {

    @FXML private ImageView logoImage;

    @FXML
    public void initialize() {
        // Load logo image
        try {
            Image image = new Image(getClass().getResourceAsStream("/com/example/carrental/images/loading2.jpg"));
            if (!image.isError()) {
                logoImage.setImage(image);
            }
        } catch (Exception e) {
            System.out.println("loading2.jpg not found in resources.");
            // Optional fallback to absolute path
            // Image image = new Image("file:C:\\CarRentalImages\\loading2.jpg");
            // logoImage.setImage(image);
        }

        // Wait 3 seconds then close this and open Login
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            Stage currentStage = (Stage) logoImage.getScene().getWindow();
            currentStage.close();

            openLoginScreen();
        });
        delay.play();
    }

    private void openLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/login.fxml"));
            Scene scene = new Scene(loader.load());

            Stage loginStage = new Stage();
            loginStage.setTitle("Car Rental System - Login");
            loginStage.setScene(scene);
            loginStage.setResizable(false);
            loginStage.centerOnScreen();
            loginStage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("ERROR: Could not load login.fxml. Check path and file name.");
        }
    }
}