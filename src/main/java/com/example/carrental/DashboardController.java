package com.example.carrental;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    // -----------------------------
    // FXML Components
    // -----------------------------
    @FXML private Button addCarsButton;
    @FXML private Button viewAvailableCarsButton;
    @FXML private Button customerDetailsButton;
    @FXML private Button bookCarButton;
    @FXML private Button generateReportButton;
    @FXML private Button returnCarButton;
    @FXML private Button myBookingsButton;      // For customers only
    @FXML private Button logoutButton;

    @FXML private AnchorPane rootPane;          // Background handled by CSS

    // -----------------------------
    // Initialization
    // -----------------------------
    @FXML
    public void initialize() {
        boolean isAdmin = Session.isAdmin();

        // Admin-only buttons
        addCarsButton.setVisible(isAdmin);
        customerDetailsButton.setVisible(isAdmin);
        generateReportButton.setVisible(isAdmin);

        // Customer-only button
        myBookingsButton.setVisible(!isAdmin);

        // Always visible buttons
        viewAvailableCarsButton.setVisible(true);
        bookCarButton.setVisible(true);
        returnCarButton.setVisible(true);
        logoutButton.setVisible(true);
    }

    // -----------------------------
    // Button Handlers
    // -----------------------------
    @FXML private void handleAddCars(ActionEvent event) throws IOException {
        switchScene(event, "add_car.fxml");
    }

    @FXML private void handleViewAvailableCars(ActionEvent event) throws IOException {
        switchScene(event, "ViewAvailableCars.fxml");
    }

    @FXML private void handleCustomerDetails(ActionEvent event) throws IOException {
        switchScene(event, "customer.fxml");
    }

    @FXML private void handleBookCar(ActionEvent event) throws IOException {
        switchScene(event, "bookcar.fxml");
    }

    @FXML private void handleGenerateReport(ActionEvent event) throws IOException {
        switchScene(event, "genrate_report.fxml");
    }

    @FXML private void handleReturnCar(ActionEvent event) throws IOException {
        System.out.println(">>> Loading return_car.fxml");
        switchScene(event, "return_car.fxml");
    }

    @FXML private void handleMyBookings(ActionEvent event) throws IOException {
        System.out.println(">>> Loading my_bookings.fxml");
        switchScene(event, "my_bookings.fxml");
    }

    @FXML private void handleLogout(ActionEvent event) throws IOException {
        Session.clear();
        switchScene(event, "login.fxml");
    }

    // -----------------------------
    // Scene Switch Helper
    // -----------------------------
    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        String fullPath = "/com/example/carrental/" + fxmlFile;
        Parent root = FXMLLoader.load(getClass().getResource(fullPath));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.centerOnScreen();
    }
}
