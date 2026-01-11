package com.example.carrental;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // 1. Load loading screen
            Parent loadingRoot = FXMLLoader.load(getClass().getResource("/com/example/carrental/Loading.fxml"));
            Scene loadingScene = new Scene(loadingRoot, 1280, 720); // Better default size
            stage.setTitle("Car Rental System - Loading");
            stage.setScene(loadingScene);
            stage.centerOnScreen();
            stage.show();

            // 2. After 3 seconds → switch to login
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> {
                try {
                    Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/example/carrental/login.fxml"));
                    Scene loginScene = new Scene(loginRoot, 1280, 720);
                    stage.setTitle("Car Rental System - Login");
                    stage.setScene(loginScene);
                    stage.centerOnScreen();
                } catch (IOException e) {
                    showErrorAlert("Failed to load login screen", e);
                }
            });
            pause.play();

        } catch (IOException e) {
            showErrorAlert("Failed to load initial loading screen", e);
        }
    }

    private void showErrorAlert(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Startup Error");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage() + "\n\nCheck console for details.");
        alert.showAndWait();
        e.printStackTrace();
    }

    public static void main(String[] args) {
        launch();
    }
}
