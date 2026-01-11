package com.example.carrental;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class generate_reportController {

    @FXML private TableView<Booking> reportTable;
    @FXML private TableColumn<Booking, Integer> customerCol;
    @FXML private TableColumn<Booking, Integer> carCol;
    @FXML private TableColumn<Booking, Integer> daysCol;
    @FXML private TableColumn<Booking, Double> costCol;
    @FXML private AnchorPane rootPane;

    private final ObservableList<Booking> bookingData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        carCol.setCellValueFactory(new PropertyValueFactory<>("carId"));
        daysCol.setCellValueFactory(new PropertyValueFactory<>("days"));
        costCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));

        loadBookings();
    }
    private void loadBookings() {
        bookingData.clear();
        try (Connection conn = DBConnection.getConnection()) {
            String query =
                    "SELECT b.customerID, b.carID, DATEDIFF(b.returndate, b.entrydate) AS days, b.total_cost " +
                            "FROM bookcar b " +
                            "ORDER BY b.booking_date DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                bookingData.add(new Booking(
                        rs.getInt("customerID"),
                        rs.getInt("carID"),
                        rs.getInt("days"),
                        rs.getDouble("total_cost")
                ));
            }

            reportTable.setItems(bookingData);

            if (!hasData) {
                showAlert(Alert.AlertType.INFORMATION, "No bookings found yet.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load report: " + e.getMessage());
        }
    }


    // Safe BACK button handler (now correctly placed inside the class)
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Always use the FULL resource path with leading "/"
            Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/com/example/carrental/Dashboard.fxml"));

            Scene scene = new Scene(dashboardRoot);
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Cannot load Dashboard.\n" +
                            "Please check if Dashboard.fxml exists in the correct path.");
        }
    }
    private void loadBackgroundImage() {
        // Recommended: dark elegant background (place in resources)
        String imagePath = "/com/example/carrental/pics/llg.png";

        try (InputStream stream = getClass().getResourceAsStream(imagePath)) {
            if (stream == null) {
                System.err.println("Background not found: " + imagePath + " → using dark fallback");
                rootPane.setStyle("-fx-background-color: #0f171e;");
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

            rootPane.setBackground(new Background(backgroundImage));

        } catch (Exception e) {
            e.printStackTrace();
            rootPane.setStyle("-fx-background-color: #0f171e;");
        }
    }
    // Helper for showing alerts (useful for errors)
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Car Rental System");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner POJO class for table rows
    public static class Booking {
        private final int customerId;
        private final int carId;
        private final int days;
        private final double totalCost;

        public Booking(int customerId, int carId, int days, double totalCost) {
            this.customerId = customerId;
            this.carId = carId;
            this.days = days;
            this.totalCost = totalCost;
        }

        public int getCustomerId() { return customerId; }
        public int getCarId() { return carId; }
        public int getDays() { return days; }
        public double getTotalCost() { return totalCost; }
    }
}