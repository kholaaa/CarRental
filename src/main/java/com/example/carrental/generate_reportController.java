package com.example.carrental;


import javafx.fxml.FXML;
import javafx.scene.control.*;
        import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class generate_reportController {

    @FXML
    private TableView<RentalReport> tableView;

    @FXML
    private TableColumn<RentalReport, Integer> bookingIdCol, carIdCol, customerIdCol;

    @FXML
    private TableColumn<RentalReport, Date> entryDateCol, returnDateCol;

    @FXML
    private TableColumn<RentalReport, Double> rentPerDayCol, totalAmountCol;

    @FXML
    private TableColumn<RentalReport, String> statusCol;

    @FXML
    private Label totalRevenueLabel, pendingCountLabel;

    @FXML
    private Button backButton, refreshButton;

    @FXML
    public void initialize() {
        // Initialize table columns
        bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("bookingID"));
        carIdCol.setCellValueFactory(new PropertyValueFactory<>("carID"));
        customerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        entryDateCol.setCellValueFactory(new PropertyValueFactory<>("entryDate"));
        returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        rentPerDayCol.setCellValueFactory(new PropertyValueFactory<>("rentPerDay"));
        totalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadReportData();

        backButton.setOnAction(e -> handleBack());
        refreshButton.setOnAction(e -> loadReportData());
    }

    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
        // Optionally open dashboard: new DashboardApp().start(new Stage());
    }

    private void loadReportData() {
        List<RentalReport> reportList = new ArrayList<>();
        double totalRevenue = 0;
        int pendingCount = 0;

        try (Connection conn = DBConnection.getConnection()) {
            String query =
                    "SELECT b.bookcarID, b.carID, b.customerID, b.entrydate, b.returndate, c.price_per_day, " +
                            "r.returnID " +
                            "FROM bookcar b " +
                            "JOIN cars c ON b.carID = c.carID " +
                            "LEFT JOIN returncar r ON b.carID = r.carID AND b.customerID = r.customerID";

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int bookingID = rs.getInt("bookcarID");
                int carID = rs.getInt("carID");
                int customerID = rs.getInt("customerID");
                Date entryDate = rs.getDate("entrydate");
                Date returnDate = rs.getDate("returndate");
                double rentPerDay = rs.getDouble("price_per_day");
                int returnID = rs.getInt("returnID");

                long days = ChronoUnit.DAYS.between(entryDate.toLocalDate(), returnDate.toLocalDate());
                if (days <= 0) days = 1;
                double total = rentPerDay * days;

                String status;
                if (rs.wasNull() || returnID == 0) {
                    status = "Pending";
                    pendingCount++;
                } else {
                    status = "Returned";
                    totalRevenue += total;
                }

                reportList.add(new RentalReport(bookingID, carID, customerID, entryDate, returnDate, rentPerDay, total, status));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        tableView.getItems().setAll(reportList);
        totalRevenueLabel.setText("Total Revenue Earned: Rs. " + String.format("%.2f", totalRevenue));
        pendingCountLabel.setText("Pending Returns: " + pendingCount);
    }
}
