package com.example.carrental;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;

import java.io.IOException;

public class DashboardController {

    @FXML private Button addCarButton;
    @FXML private Button viewCarButton;
    @FXML private Button customerButton;
    @FXML private Button bookCarButton;
    @FXML private Button returnCarButton;
    @FXML private Button reportButton;
    @FXML private Button logoutButton;

    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open " + title + ":\n" + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        addCarButton.setOnAction(e -> openWindow("/com/example/carrental/add_car.fxml", "Add Car"));
        viewCarButton.setOnAction(e -> openWindow("/com/example/carrental/ViewAvailableCars.fxml", "View Available Cars"));
        customerButton.setOnAction(e -> openWindow("/com/example/carrental/customer.fxml", "Customer Details"));
        bookCarButton.setOnAction(e -> openWindow("/com/example/carrental/bookcar.fxml", "Book Car"));
        returnCarButton.setOnAction(e -> openWindow("/com/example/carrental/return_car.fxml", "Return Car"));
        reportButton.setOnAction(e -> openWindow("/com/example/carrental/genrate_report.fxml", "Generate Report"));
        logoutButton.setOnAction(this::handleLogout);
    }

    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText("Logged out successfully.");
        alert.showAndWait();

        // Close the entire application or just dashboard
        logoutButton.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}