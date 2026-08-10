package com.example.carrental;

import javafx.collections.FXCollections;
import com.example.carrental.chat.FloatingChatButton;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
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
    @FXML private TableColumn<Booking, String> customerNameCol;
    @FXML private TableColumn<Booking, Integer> carCol;
    @FXML private TableColumn<Booking, String> startDateCol;
    @FXML private TableColumn<Booking, String> returnDateCol;
    @FXML private TableColumn<Booking, String> statusCol;
    @FXML private TableColumn<Booking, String> conditionCol;
    @FXML private TableColumn<Booking, Integer> fuelCol;
    @FXML private TableColumn<Booking, Integer> daysCol;
    @FXML private TableColumn<Booking, Double> costCol;
    @FXML private Label forecastLabel;
    @FXML private AnchorPane rootPane;

    private final ObservableList<Booking> bookingData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadBackgroundImage();
        FloatingChatButton.install(rootPane);

        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        carCol.setCellValueFactory(new PropertyValueFactory<>("carId"));
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        conditionCol.setCellValueFactory(new PropertyValueFactory<>("carCondition"));
        fuelCol.setCellValueFactory(new PropertyValueFactory<>("fuelLevel"));
        daysCol.setCellValueFactory(new PropertyValueFactory<>("days"));
        costCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));

        loadBookings();
        computeForecast();
    }

    private void loadBookings() {
        bookingData.clear();
        try (Connection conn = DBConnection.getConnection()) {
            String query =
                    "SELECT u.name AS customer_name, b.carID, b.entrydate, b.returndate, b.returned, " +
                            "DATEDIFF(b.returndate, b.entrydate) AS days, b.total_cost, " +
                            "r.fuellevel, r.carcondition " +
                            "FROM bookcar b " +
                            "LEFT JOIN users u ON b.customerID = u.id " +
                            "LEFT JOIN returncar r ON r.carID = b.carID AND r.customerID = b.customerID " +
                            "ORDER BY b.booking_date DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                boolean returned = rs.getBoolean("returned");
                bookingData.add(new Booking(
                        rs.getString("customer_name"),
                        rs.getInt("carID"),
                        rs.getString("entrydate"),
                        rs.getString("returndate"),
                        returned ? "Returned" : "Active",
                        rs.getString("carcondition"),
                        rs.getObject("fuellevel") != null ? rs.getInt("fuellevel") : null,
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

    private void computeForecast() {
        try (Connection conn = DBConnection.getConnection()) {
            double rev90  = querySum(conn, "SELECT COALESCE(SUM(total_cost),0) FROM bookcar WHERE booking_date >= DATE_SUB(CURDATE(), INTERVAL 90 DAY)");
            double rev30  = querySum(conn, "SELECT COALESCE(SUM(total_cost),0) FROM bookcar WHERE booking_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)");
            double revPrev = querySum(conn, "SELECT COALESCE(SUM(total_cost),0) FROM bookcar WHERE booking_date >= DATE_SUB(CURDATE(), INTERVAL 60 DAY) AND booking_date < DATE_SUB(CURDATE(), INTERVAL 30 DAY)");
            long active = queryCount(conn, "SELECT COUNT(*) FROM bookcar WHERE returned IS NULL OR returned = 0");

            double dailyAvg = rev90 / 90.0;
            double growth = revPrev > 0 ? (rev30 - revPrev) / revPrev : 0.0;
            double adjGrowth = Math.max(-0.10, Math.min(0.10, growth * 0.5));
            double forecast30 = dailyAvg * 30.0 * (1.0 + adjGrowth);

            String trend = adjGrowth > 0.02 ? "up" : adjGrowth < -0.02 ? "down" : "stable";
            forecastLabel.setText(String.format(
                    "AI REVENUE FORECAST  |  Avg daily booked: %.0f PKR  |  30-day trend: %s (%+.1f%%)  |  Next 30 days: %.0f PKR  |  Active bookings: %d",
                    dailyAvg, trend, adjGrowth * 100, forecast30, active));
        } catch (SQLException e) {
            e.printStackTrace();
            forecastLabel.setText("Could not compute revenue forecast.");
        }
    }

    private double querySum(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    private long queryCount(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }



    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();


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
        private final String customerName;
        private final int carId;
        private final String startDate;
        private final String returnDate;
        private final String status;
        private final String carCondition;
        private final Integer fuelLevel;
        private final int days;
        private final double totalCost;

        public Booking(String customerName, int carId, String startDate, String returnDate,
                       String status, String carCondition, Integer fuelLevel, int days, double totalCost) {
            this.customerName = customerName;
            this.carId = carId;
            this.startDate = startDate;
            this.returnDate = returnDate;
            this.status = status;
            this.carCondition = carCondition == null ? "-" : carCondition;
            this.fuelLevel = fuelLevel;
            this.days = days;
            this.totalCost = totalCost;
        }

        public String getCustomerName() { return customerName; }
        public int getCarId() { return carId; }
        public String getStartDate() { return startDate; }
        public String getReturnDate() { return returnDate; }
        public String getStatus() { return status; }
        public String getCarCondition() { return carCondition; }
        public Integer getFuelLevel() { return fuelLevel; }
        public int getDays() { return days; }
        public double getTotalCost() { return totalCost; }
    }
}
