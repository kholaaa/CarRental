package com.example.carrental;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class DashboardController {

    @FXML private Button addCarsButton;
    @FXML private Button viewAvailableCarsButton;
    @FXML private Button customerDetailsButton;
    @FXML private Button bookCarButton;
    @FXML private Button generateReportButton;
    @FXML private Button returnCarButton;
    @FXML private Button myBookingsButton;      // For customers only
    @FXML private Button logoutButton;

    @FXML private AnchorPane rootPane;

    @FXML
    public void initialize() {
        // Role-based visibility
        boolean isAdmin = Session.isAdmin();

        // Admin-only buttons
        addCarsButton.setVisible(isAdmin);
        customerDetailsButton.setVisible(isAdmin);
        generateReportButton.setVisible(isAdmin);

        // Customer-only button
        myBookingsButton.setVisible(!isAdmin);

        // Always visible
        viewAvailableCarsButton.setVisible(true);
        bookCarButton.setVisible(true);
        returnCarButton.setVisible(true);
        logoutButton.setVisible(true);

        // Load background
        loadUniqueDashboardBackground();
    }

    private void loadUniqueDashboardBackground() {
        String imagePath = "com/example/carrental/pics/car2.png";

        try (InputStream stream = getClass().getResourceAsStream(imagePath)) {
            if (stream == null) {
                System.err.println("Background missing: " + imagePath + " → fallback gradient");
                rootPane.setStyle("-fx-background-color: linear-gradient(to bottom, #0f2027, #203a43, #2c5364);");
                return;
            }

            Image bgImage = new Image(stream);
            BackgroundImage backgroundImage = new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
            );

            BackgroundFill overlay = new BackgroundFill(
                    javafx.scene.paint.Color.rgb(0, 0, 0, 0.45),
                    null, null
            );

            rootPane.setBackground(new Background(
                    new BackgroundFill[]{overlay},
                    new BackgroundImage[]{backgroundImage}
            ));

        } catch (Exception e) {
            e.printStackTrace();
            rootPane.setStyle("-fx-background-color: linear-gradient(to bottom, #0f2027, #203a43, #2c5364);");
        }
    }

    // Navigation methods
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
        switchScene(event, "return_car.fxml");
    }

    @FXML private void handleMyBookings(ActionEvent event) throws IOException {
        switchScene(event, "my_bookings.fxml");
    }

    @FXML private void handleLogout(ActionEvent event) throws IOException {
        Session.clear();
        switchScene(event, "login.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        String fullPath = "/com/example/carrental/" + fxmlFile;

        // FIXED: correct string path with quotes
        Parent root = FXMLLoader.load(getClass().getResource(fullPath));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.centerOnScreen();
    }
}