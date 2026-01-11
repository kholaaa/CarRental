package com.example.carrental;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import java.sql.*;

public class RentalReport {

    @FXML private TableView<Booking> reportTable;
    @FXML private TableColumn<Booking, String> customerCol, carCol, dateCol;
    @FXML private TableColumn<Booking, Integer> daysCol;
    @FXML private AnchorPane rootPane;

    private ObservableList<Booking> bookingData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setDarkBackground(rootPane, "report_bg.jpg");  // Now this will work!

        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        carCol.setCellValueFactory(new PropertyValueFactory<>("carId"));
        daysCol.setCellValueFactory(new PropertyValueFactory<>("days"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));

        loadRentalReport();
    }

    // ADD THIS METHOD - This was missing!
    private void setDarkBackground(AnchorPane rootPane, String imageName) {
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/com/example/carrental/pics/" + imageName));
            BackgroundImage backgroundImage = new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, true, true)
            );
            rootPane.setBackground(new Background(backgroundImage));
        } catch (Exception e) {
            // Fallback to solid dark if image not found
            rootPane.setStyle("-fx-background-color: #222222;");
        }
    }

    private void loadRentalReport() {
        bookingData.clear(); // Prevents duplicates if reloaded
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM bookings"; // Change if your table name is different
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                bookingData.add(new Booking(
                        rs.getString("customer_id"),
                        rs.getString("car_id"),
                        rs.getInt("rental_days"),
                        rs.getString("booking_date") // Adjust column name if needed
                ));
            }
            reportTable.setItems(bookingData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inner Booking class - PERFECTLY FINE here, no need for separate file
    class Booking {
        private final String customerId, carId, bookingDate;
        private final int days;

        public Booking(String customerId, String carId, int days, String bookingDate) {
            this.customerId = customerId;
            this.carId = carId;
            this.days = days;
            this.bookingDate = bookingDate;
        }

        public String getCustomerId() { return customerId; }
        public String getCarId() { return carId; }
        public int getDays() { return days; }
        public String getBookingDate() { return bookingDate; }
    }
}