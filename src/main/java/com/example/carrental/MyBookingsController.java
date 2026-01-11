package com.example.carrental;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class MyBookingsController {

    @FXML private TableView<BookingRecord> bookingsTable;
    @FXML private TableColumn<BookingRecord, Integer> carIdCol;
    @FXML private TableColumn<BookingRecord, String> modelCol;
    @FXML private TableColumn<BookingRecord, String> fromCol;
    @FXML private TableColumn<BookingRecord, String> toCol;
    @FXML private TableColumn<BookingRecord, Double> costCol;
    @FXML private TableColumn<BookingRecord, String> statusCol;
    @FXML private TableColumn<BookingRecord, Integer> returnFuelCol;
    @FXML private TableColumn<BookingRecord, String> returnConditionCol;

    private final ObservableList<BookingRecord> bookingData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up columns
        carIdCol.setCellValueFactory(new PropertyValueFactory<>("carId"));
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        fromCol.setCellValueFactory(new PropertyValueFactory<>("fromDate"));
        toCol.setCellValueFactory(new PropertyValueFactory<>("toDate"));
        costCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        returnFuelCol.setCellValueFactory(new PropertyValueFactory<>("returnFuel"));
        returnConditionCol.setCellValueFactory(new PropertyValueFactory<>("returnCondition"));

        loadMyBookings();
    }

    private void loadMyBookings() {
        bookingData.clear();
        int currentUserId = Session.getUserId();

        try (Connection conn = DBConnection.getConnection()) {
            String query = """
                SELECT 
                    b.carID, c.carmodel, b.entrydate, b.returndate, b.total_cost,
                    r.fuellevel AS return_fuel, r.carcondition AS return_condition
                FROM bookcar b
                JOIN cars c ON b.carID = c.carID
                LEFT JOIN returncar r ON b.carID = r.carID AND b.customerID = r.customerID
                WHERE b.customerID = ?
                ORDER BY b.booking_date DESC
            """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String status = (rs.getObject("return_fuel") == null) ? "Active" : "Returned";
                bookingData.add(new BookingRecord(
                        rs.getInt("carID"),
                        rs.getString("carmodel"),
                        rs.getDate("entrydate").toString(),
                        rs.getDate("returndate").toString(),
                        rs.getDouble("total_cost"),
                        status,
                        rs.getObject("return_fuel") != null ? rs.getInt("return_fuel") : null,
                        rs.getString("return_condition")
                ));
            }

            bookingsTable.setItems(bookingData);

        } catch (SQLException e) {
            e.printStackTrace();
            // You can add alert here if you want
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/com/example/carrental/Dashboard.fxml"));
        Scene scene = new Scene(dashboardRoot);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    // Inner class for table rows
    public static class BookingRecord {
        private final int carId;
        private final String model;
        private final String fromDate;
        private final String toDate;
        private final double totalCost;
        private final String status;
        private final Integer returnFuel;
        private final String returnCondition;

        public BookingRecord(int carId, String model, String fromDate, String toDate,
                             double totalCost, String status, Integer returnFuel, String returnCondition) {
            this.carId = carId;
            this.model = model;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.totalCost = totalCost;
            this.status = status;
            this.returnFuel = returnFuel;
            this.returnCondition = returnCondition != null ? returnCondition : "";
        }

        public int getCarId() { return carId; }
        public String getModel() { return model; }
        public String getFromDate() { return fromDate; }
        public String getToDate() { return toDate; }
        public double getTotalCost() { return totalCost; }
        public String getStatus() { return status; }
        public Integer getReturnFuel() { return returnFuel; }
        public String getReturnCondition() { return returnCondition; }
    }
}