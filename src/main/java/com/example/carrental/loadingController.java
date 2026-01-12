package com.example.carrental;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.InputStream;

public class loadingController {

    @FXML
    private ImageView logoImageView;

    @FXML
    private AnchorPane rootPane;

    @FXML
    public void initialize() {

        rootPane.setStyle("-fx-background-color: black;");


        String imagePath = "/pics/img.png";

        try (InputStream inputStream = loadingController.class.getResourceAsStream(imagePath)) {
            if (inputStream == null) {
                System.err.println("ERROR: Image not found at path: " + imagePath);
                System.err.println("Check that file exists in: src/main/resources/pics/llg.png");

                logoImageView.setStyle("-fx-background-color: #333333;");
            } else {
                Image logo = new Image(inputStream);
                logoImageView.setImage(logo);
                System.out.println("Loading logo loaded successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load logo image");
        }

    }
}