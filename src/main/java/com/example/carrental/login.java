package com.example.carrental;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class login extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("login.fxml")
        );

        stage.setScene(new Scene(loader.load(), 1920, 1080));
        stage.setTitle("Car Rental System - Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
