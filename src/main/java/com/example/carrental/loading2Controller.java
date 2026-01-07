package com.example.carrental;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
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
            // Optional fallback
            // logoImage.setImage(new Image("file:C:\\CarRentalImages\\loading2.jpg"));
        }

        // 3-second delay then open Dashboard
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> openDashboard());
        delay.play();
    }

    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/Dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Car Rental Dashboard");
            dashboardStage.setScene(scene);
            dashboardStage.setMaximized(true);
            dashboardStage.show();

            // Close loading2
            Stage currentStage = (Stage) logoImage.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERROR: Could not load Dashboard.fxml");
        }
    }

}