package com.example.carrental;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;

public class loading2Controller {

    @FXML
    private ImageView logoImage;

    @FXML
    public void initialize() {
        // Load the logo image
        try {
            Image image = new Image(getClass().getResourceAsStream("pics/load1.jpg"));
            if (image.isError()) {
                throw new Exception("Image failed to load");
            }
            logoImage.setImage(image);
        } catch (Exception e) {
            System.out.println("Warning: loading2.jpg not found in resources/com/example/carrental/pics/");
            // Optional fallback during development (remove in production)
            // logoImage.setImage(new Image("file:C:\\CarRentalImages\\loading2.jpg"));
        }

        // Wait 3 seconds then try to open the Dashboard
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> openDashboard());
        delay.play();
    }

    private void openDashboard() {
        try {
            // First, check if the FXML file can be found
            URL fxmlUrl = getClass().getResource("Dashboard.fxml");
            if (fxmlUrl == null) {
                System.err.println("CRITICAL: Dashboard.fxml NOT FOUND!");
                System.err.println("Expected location: src/main/resources/com/example/carrental/Dashboard.fxml");

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("File Not Found");
                    alert.setHeaderText(null);
                    alert.setContentText("Dashboard.fxml could not be found.\n\n" +
                            "Please ensure the file is in:\n" +
                            "src/main/resources/com/example/carrental/Dashboard.fxml");
                    alert.showAndWait();
                });
                return;
            }

            // Load the Dashboard
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load());

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Car Rental Dashboard");
            dashboardStage.setScene(scene);
            dashboardStage.setMaximized(true);
            dashboardStage.show();

            // Close the current Loading2 window
            Stage currentStage = (Stage) logoImage.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Loading Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to load the Dashboard.\n\n" +
                        "Error: " + e.getMessage() + "\n\n" +
                        "Check console for full details.");
                alert.showAndWait();
            });
        }
    }
}