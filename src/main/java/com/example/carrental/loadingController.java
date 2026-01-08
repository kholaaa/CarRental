package com.example.carrental;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class loadingController {

    @FXML private ImageView backgroundImage;

    @FXML
    public void initialize() {
        try {
            backgroundImage.setImage(new Image(getClass().getResourceAsStream("/com/example/carrental/pics/loading.jpg")));
        } catch (Exception e) {
            System.out.println("Loading background image not found.");
        }

        // Wait 2 seconds then open login
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> openLogin());
        delay.play();
    }

    private void openLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            Stage stage = (Stage) backgroundImage.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login - Car Rental System");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
