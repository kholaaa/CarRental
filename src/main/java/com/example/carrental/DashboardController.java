package com.example.carrental;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML private Button addCarButton;
    @FXML private Button viewCarButton;
    @FXML private Button customerButton;
    @FXML private Button bookCarButton;
    @FXML private Button returnCarButton;
    @FXML private Button reportButton;
    @FXML private Button logoutButton;

    @FXML private ImageView backgroundImage;  // Important!

    @FXML
    public void initialize() {
        // Load background image programmatically (reliable method)
        // Load background image with RELATIVE path (correct for subfolders)


        try {
            System.out.println("Trying to load image from: pics/img.png");
            Image img = new Image(getClass().getResourceAsStream("/com/example/carrental/pics/img.png"));

            if (img.isError()) {
                System.err.println("Image is loaded but has error (corrupt file?)");
                throw new Exception("Image has error");
            }

            if (img.getWidth() <= 0 || img.getHeight() <= 0) {
                throw new Exception("Image dimensions are zero - invalid");
            }

            backgroundImage.setImage(img);
            System.out.println("SUCCESS: new.png loaded! Width=" + img.getWidth() + ", Height=" + img.getHeight());
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load background.png");
            System.err.println("Path tried: pics/bg.jpg (relative from DashboardController)");
            System.err.println("Full expected location: src/main/resources/com/example/carrental/pics/img.png");
            e.printStackTrace();
            // Fallback to solid color so you know something happened
            backgroundImage.setStyle("-fx-background-color: #1e3a8a;"); // Deep blue fallback
        }

        // Button actions
        addCarButton.setOnAction(e -> openWindow("add_car.fxml", "Add Car"));
        viewCarButton.setOnAction(e -> openWindow("ViewAvailableCars.fxml", "View Available Cars"));
        customerButton.setOnAction(e -> openWindow("customer.fxml", "Customer Details"));
        bookCarButton.setOnAction(e -> openWindow("bookcar.fxml", "Book Car"));
        returnCarButton.setOnAction(e -> openWindow("return_car.fxml", "Return Car"));
        reportButton.setOnAction(e -> openWindow("generate_report.fxml", "Generate Report"));
        logoutButton.setOnAction(e -> handleLogout());
    }

    private void openWindow(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            if (loader.getLocation() == null) {
                showAlert("File Not Found", "FXML file not found: " + fxmlFile +
                        "\n\nEnsure it exists in src/main/resources/com/example/carrental/");
                return;
            }
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open " + title + ":\n" + e.getMessage());
        }
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText("You have been logged out successfully.");
        alert.showAndWait();

        // Close dashboard
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}