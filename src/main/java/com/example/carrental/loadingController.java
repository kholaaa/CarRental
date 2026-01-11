package com.example.carrental;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.InputStream;

public class loadingController {

    @FXML
    private ImageView logoImageView;  // must exist in Loading.fxml with fx:id="logoImageView"

    @FXML
    private AnchorPane rootPane;      // must exist in Loading.fxml with fx:id="rootPane"

    @FXML
    public void initialize() {
        // 1. Set black background
        rootPane.setStyle("-fx-background-color: black;");

        // 2. Load image SAFELY
        String imagePath = "/pics/img.png";   // ← most important part: path must start with /

        try (InputStream inputStream = loadingController.class.getResourceAsStream(imagePath)) {
            if (inputStream == null) {
                System.err.println("ERROR: Image not found at path: " + imagePath);
                System.err.println("Check that file exists in: src/main/resources/pics/llg.png");
                // You can leave logo empty or set fallback color/image
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

        // Optional: remove this useless line from your original code
        // loadingController.class.getResourceAsStream("/pics/img.jpg");  ← does nothing
    }
}