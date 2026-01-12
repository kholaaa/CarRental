package com.example.carrental;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
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
import java.io.InputStream;
import java.net.URL;
import java.io.IOException;
import javafx.scene.image.Image;
public class DashboardController {

    // FXML Components
    @FXML private AnchorPane rootPane;
    @FXML private Button addCarsButton;
    @FXML private Button viewAvailableCarsButton;
    @FXML private Button bookCarButton;
    @FXML private Button returnCarButton;
    @FXML private Button myBookingsButton;
    @FXML private Button customerDetailsButton;
    @FXML private Button generateReportButton;
    @FXML private Button logoutButton;
    @FXML private Button viewTermsButton;


    @FXML
    public void initialize() {

        boolean isAdmin = Session.isAdmin();

        addCarsButton.setVisible(isAdmin);
        addCarsButton.setManaged(isAdmin);
        customerDetailsButton.setVisible(isAdmin);
        customerDetailsButton.setManaged(isAdmin);
        generateReportButton.setVisible(isAdmin);
        generateReportButton.setManaged(isAdmin);

        // Customer-only feature
        myBookingsButton.setVisible(!isAdmin);
        myBookingsButton.setManaged(!isAdmin);

        // Common features (always visible)
        viewAvailableCarsButton.setVisible(true);
        bookCarButton.setVisible(true);
        returnCarButton.setVisible(true);
        logoutButton.setVisible(true);
        viewTermsButton.setVisible(true);  // A
       System.out.println("Trying to load background image...");
        System.out.println("Resource exists: " +
                getClass().getResource("llg.png") != null);
    }

    @FXML
    private void handleViewAvailableCars(ActionEvent event) throws IOException {
        switchScene(event, "ViewAvailableCars.fxml");
    }

    @FXML
    private void handleBookCar(ActionEvent event) throws IOException {
        switchScene(event, "bookcar.fxml");
    }

    @FXML
    private void handleReturnCar(ActionEvent event) throws IOException {
        switchScene(event, "return_car.fxml");
    }

    @FXML
    private void handleAddCars(ActionEvent event) throws IOException {
        switchScene(event, "add_car.fxml");
    }

    @FXML
    private void handleCustomerDetails(ActionEvent event) throws IOException {
        switchScene(event, "customer.fxml");
    }

    @FXML
    private void handleMyBookings(ActionEvent event) throws IOException {
        switchScene(event, "my_bookings.fxml");
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) throws IOException {
        switchScene(event, "genrate_report.fxml");
    }

    @FXML
    private void handleViewTerms(ActionEvent event) throws IOException {
        switchScene(event, "terms_conditions.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        Session.clear();  // Clear user session
        switchScene(event, "login.fxml");
    }


    private void switchScene(ActionEvent event, String fxmlFileName) throws IOException {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            String resourcePath = "/com/example/carrental/" + fxmlFileName;
            Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load scene: " + fxmlFileName);
            e.printStackTrace();

        }
    }
}