package com.example.carrental;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

 class bookCar extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BookCar.fxml"));
            Scene scene = new Scene(loader.load());

            primaryStage.setTitle("Book Car");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Main method to launch JavaFX
    public static void main(String[] args) {
        launch(args);
    }
}
