package com.example.carrental;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class loading2Controller {

    @FXML private ImageView backgroundImage;
    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        try {
            backgroundImage.setImage(new Image(getClass().getResourceAsStream("/com/example/carrental/pics/loading2.jpg")));
        } catch (Exception e) {
            System.out.println("Loading2 background not found.");
        }

        welcomeLabel.setText("Welcome to Car Rental System");

        // Wait 3 seconds then open dashboard
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> openDashboard());
        delay.play();
    }

    private void openDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/carrental/dashboard.fxml"));
            Stage stage = (Stage) backgroundImage.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Dashboard - Car Rental System");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
