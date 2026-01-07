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
        // Load logo image from resources
        try {
            Image image = new Image(getClass().getResourceAsStream("/com/example/carrental/images/loading2.jpg"));
            if (image.isError()) {
                throw new Exception("Image load error");
            }
            logoImage.setImage(image);
        } catch (Exception e) {
            System.out.println("Warning: loading2.jpg not found in resources.");
            // Optional fallback for development
            // logoImage.setImage(new Image("file:C:\\CarRentalImages\\loading2.jpg"));
        }

        // Wait 3 seconds then open Dashboard
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> openDashboard());
        delay.play();
    }

    private void openDashboard() {
        try {
            // CORRECT PATH: relative to the controller class
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));

            Scene scene = new Scene(loader.load());

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Car Rental Dashboard");
            dashboardStage.setScene(scene);
            dashboardStage.setMaximized(true);
            dashboardStage.show();

            // Close the Loading2 window
            Stage currentStage = (Stage) logoImage.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERROR: Could not load Dashboard.fxml");
            System.err.println("Make sure Dashboard.fxml is in src/main/resources/com/example/carrental/");
        }
    }
}