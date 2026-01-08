package com.example.carrental;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private ImageView backgroundImage;
    @FXML private Button logoutButton;
    @FXML private Button bookCarButton;
    @FXML private Button addCarButton;
    @FXML private Button viewCarButton;
    @FXML private Button customerButton;
    @FXML private Button returnCarButton;
    @FXML private Button reportButton;

    @FXML
    public void initialize() {
        // Load dashboard background
        try {
            backgroundImage.setImage(
                    new Image(getClass().getResourceAsStream("/com/example/carrental/pics/bg.png"))
            );
        } catch (Exception e) {
            System.out.println("Dashboard background not found.");
        }
    }

    @FXML
    private void handleAddCar() {
        loadFXML("/com/example/carrental/addCar.fxml", addCarButton);
    }

    @FXML
    private void handleViewCar() {
        loadFXML("/com/example/carrental/viewCar.fxml", viewCarButton);
    }

    @FXML
    private void handleCustomer() {
        loadFXML("/com/example/carrental/customer.fxml", customerButton);
    }

    @FXML
    private void handleBookCar() {
        loadFXML("/com/example/carrental/bookcar.fxml", bookCarButton);
    }

    @FXML
    private void handleReturnCar() {
        loadFXML("/com/example/carrental/returnCar.fxml", returnCarButton);
    }

    @FXML
    private void handleReport() {
        loadFXML("/com/example/carrental/report.fxml", reportButton);
    }

    @FXML
    private void handleLogout() {
        // Close current stage (logout)
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
    }

    // Utility method to load FXML screens
    private void loadFXML(String fxmlPath, Button button) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) button.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Car Rental System");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
