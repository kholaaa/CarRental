package com.example.carrental;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class loadingController {

    @FXML private ImageView backgroundImage;
    @FXML private ProgressIndicator progressIndicator;

    @FXML
    public void initialize() {
        // Try to load image from resources first (recommended)
        try {
            Image img = new Image(getClass().getResourceAsStream("/com/example/carrental/images/loading page 1.jpg"));
            if (!img.isError()) {
                backgroundImage.setImage(img);
            }
        } catch (Exception e) {
            // Fallback to absolute path if resource not found
            Image img = new Image("file:C:\\CarRentalImages\\loading page 1.jpg");
            backgroundImage.setImage(img);
        }

        // Optional: make progress indicator visible
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }

        // Delay 2 seconds then go to loading2
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> openNextScreen());
        delay.play();
    }

    private void openNextScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/loading2.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Loading...");
            stage.show();

            // Close current window
            Stage currentStage = (Stage) backgroundImage.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERROR: Could not load loading2.fxml. Check if file exists at /com/example/carrental/loading2.fxml");
        }
    }
}