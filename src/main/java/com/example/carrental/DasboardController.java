package com.example.carrental;



import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;

public class DashboardController {

    @FXML
    private Button addCarButton;

    @FXML
    private Button viewCarButton;

    @FXML
    private Button customerButton;

    @FXML
    private Button bookCarButton;

    @FXML
    private Button returnCarButton;

    @FXML
    private Button reportButton;

    @FXML
    private Button logoutButton;

    // Initialize method (optional)
    @FXML
    public void initialize() {
        addCarButton.setOnAction(this::handleAddCar);
        viewCarButton.setOnAction(this::handleViewCars);
        customerButton.setOnAction(this::handleCustomer);
        bookCarButton.setOnAction(this::handleBookCar);
        returnCarButton.setOnAction(this::handleReturnCar);
        reportButton.setOnAction(this::handleGenerateReport);
        logoutButton.setOnAction(this::handleLogout);
    }

    private void handleAddCar(ActionEvent event) {
        // Open Add Car window
        new add_car(); // You need to convert add_car to JavaFX as well
    }

    private void handleViewCars(ActionEvent event) {
        new ViewAvailableCars(); // JavaFX version
    }

    private void handleCustomer(ActionEvent event) {
        new customer(); // JavaFX version
    }

    private void handleBookCar(ActionEvent event) {
        new bookcar(); // JavaFX version
    }

    private void handleReturnCar(ActionEvent event) {
        new return_car(); // JavaFX version
    }

    private void handleGenerateReport(ActionEvent event) {
        new generate_report(); // JavaFX version
    }

    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText("Logged out successfully.");
        alert.showAndWait();

        // Close dashboard
        logoutButton.getScene().getWindow().hide();
    }
}
