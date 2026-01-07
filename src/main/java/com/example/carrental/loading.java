package com.example.carrental;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class loading extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/loading.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Loading");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true); // fullscreen
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}