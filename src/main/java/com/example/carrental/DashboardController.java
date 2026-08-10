package com.example.carrental;

import com.example.carrental.ai.RecommendationEngine;
import com.example.carrental.chat.FloatingChatButton;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
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
    @FXML private Button deleteCarButton;
    @FXML private Button logoutButton;
    @FXML private Button viewTermsButton;

    @FXML private Label recommendationsLabel;
    @FXML private ListView<String> recommendationsList;
    @FXML private Label churnLabel;
    @FXML private ListView<String> churnList;


    @FXML
    public void initialize() {

        boolean isAdmin = Session.isAdmin();

        addCarsButton.setVisible(isAdmin);
        addCarsButton.setManaged(isAdmin);
        customerDetailsButton.setVisible(isAdmin);
        customerDetailsButton.setManaged(isAdmin);
        generateReportButton.setVisible(isAdmin);
        generateReportButton.setManaged(isAdmin);
        deleteCarButton.setVisible(isAdmin);
        deleteCarButton.setManaged(isAdmin);

        // Customer-only feature
        myBookingsButton.setVisible(!isAdmin);
        myBookingsButton.setManaged(!isAdmin);

        // Book a Car is also customer-only
        bookCarButton.setVisible(!isAdmin);
        bookCarButton.setManaged(!isAdmin);

        // Return a Car is also customer-only
        returnCarButton.setVisible(!isAdmin);
        returnCarButton.setManaged(!isAdmin);

        // Common features (always visible)
        viewAvailableCarsButton.setVisible(true);
        logoutButton.setVisible(true);
        viewTermsButton.setVisible(true);

        // AI recommendations are only meaningful for customers
        recommendationsLabel.setVisible(!isAdmin);
        recommendationsLabel.setManaged(!isAdmin);
        recommendationsList.setVisible(!isAdmin);
        recommendationsList.setManaged(!isAdmin);
        if (!isAdmin) {
            loadRecommendations();
        } else {
            // Admin sees churn alerts instead of recommendations
            churnLabel.setVisible(true);
            churnLabel.setManaged(true);
            churnList.setVisible(true);
            churnList.setManaged(true);
            loadChurnAlerts();
        }

        System.out.println("Trying to load background image...");
        System.out.println("Resource exists: " +
                getClass().getResource("llg.png") != null);
        System.out.println("CSS resolved to: " + getClass().getResource("dashboard.css"));
        System.out.println("FXML resolved to: " + getClass().getResource("Dashboard.fxml"));

        loadBackgroundImage();

        FloatingChatButton.install(rootPane);
    }

    private void loadBackgroundImage() {
        String path = "/com/example/carrental/pics/llg.png";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                rootPane.setStyle("-fx-background-color: #111111;");
                return;
            }
            Image bgImage = new Image(is);
            BackgroundImage bg = new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
            );
            rootPane.setBackground(new Background(bg));
        } catch (Exception e) {
            rootPane.setStyle("-fx-background-color: #111111;");
        }
    }

    private void loadRecommendations() {
        List<RecommendationEngine.ScoredCar> recs =
                RecommendationEngine.recommend(Session.getUserId(), 3);

        ObservableList<String> items = FXCollections.observableArrayList();
        for (RecommendationEngine.ScoredCar r : recs) {
            items.add(String.format("%s (%s) — %.0f PKR/day", r.model, r.type, r.pricePerDay));
        }

        if (items.isEmpty()) {
            items.add("No recommendations yet — book a car to get personalized suggestions!");
        }

        recommendationsList.setItems(items);
    }

    private void loadChurnAlerts() {
        String sql = """
            SELECT u.name,
                   COUNT(b.bookcarID) AS totalBookings,
                   MAX(COALESCE(b.actual_return_date, b.booking_date)) AS lastActivity
            FROM users u
            JOIN bookcar b ON b.customerID = u.id
            WHERE u.role = 'customer'
            GROUP BY u.id, u.name
            HAVING MAX(COALESCE(b.actual_return_date, b.booking_date))
                   < DATE_SUB(CURDATE(), INTERVAL 90 DAY)
            ORDER BY lastActivity ASC
            LIMIT 12
        """;

        ObservableList<String> items = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(String.format("%s — %d booking(s), last activity %s",
                        rs.getString("name"),
                        rs.getInt("totalBookings"),
                        rs.getDate("lastActivity")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (items.isEmpty()) {
            items.add("No churn risk detected — all customers are active in the last 90 days.");
        }

        churnList.setItems(items);
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
    private void handleDeleteCar(ActionEvent event) throws IOException {
        switchScene(event, "delete_car.fxml");
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

            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Navigation Error");
                alert.setHeaderText(null);
                alert.setContentText("File not found: " + resourcePath);
                alert.showAndWait();
                return;
            }

            Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to load: " + fxmlFileName);
            alert.setContentText(String.valueOf(e));
            alert.showAndWait();
        }
    }
}