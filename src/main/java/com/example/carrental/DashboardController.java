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
        try {
            Image img = new Image(getClass().getResourceAsStream("/com/example/carrental/pics/bg.jpg"));
            if (img.isError()) {
                throw new Exception("Image load failed");
            }
            backgroundImage.setImage(img);
        } catch (Exception e) {
            System.err.println("WARNING: Could not load bg.jpg!");
            System.err.println("Checked path: /com/example/carrental/pics/bg.jpg");
            System.err.println("Make sure the file exists in src/main/resources/com/example/carrental/pics/bg.jpg");
            e.printStackTrace();
            // Fallback dark background
            backgroundImage.setStyle("-fx-background-color: #16213e;");
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