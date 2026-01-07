package com.example.carrental;

public class loadingController {

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ProgressIndicator;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

    public class loadingController {

        @FXML
        private ImageView backgroundImage;

        @FXML
        private ProgressIndicator progressIndicator;

        @FXML
        public void initialize() {
            // Set the background image
            Image img = new Image("file:C:\\CarRentalImages\\loading page 1.jpg"); // Local image
            backgroundImage.setImage(img);

            // Wait for 1 second, then move to loading2
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
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
                stage.show();

                // Close current loading stage
                Stage currentStage = (Stage) backgroundImage.getScene().getWindow();
                currentStage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
