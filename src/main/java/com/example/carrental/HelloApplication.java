package com.example.carrental;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/loading.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        stage.setScene(scene);
        stage.setTitle("Car Rental System");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
