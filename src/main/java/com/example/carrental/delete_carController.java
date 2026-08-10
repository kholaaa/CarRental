package com.example.carrental;

import com.example.carrental.chat.FloatingChatButton;

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
import java.sql.*;

public class delete_carController {

    @FXML private TableView<Car> carsTable;
    @FXML private TableColumn<Car, Integer> carIdCol;
    @FXML private TableColumn<Car, String> modelCol;
    @FXML private TableColumn<Car, String> typeCol;
    @FXML private TableColumn<Car, String> colourCol;
    @FXML private TableColumn<Car, Double> priceCol;
    @FXML private TableColumn<Car, String> availabilityCol;
    @FXML private AnchorPane rootPane;

    private final ObservableList<Car> carData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setBackground();
        FloatingChatButton.install(rootPane);

        carIdCol.setCellValueFactory(new PropertyValueFactory<>("carId"));
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        colourCol.setCellValueFactory(new PropertyValueFactory<>("colour"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        availabilityCol.setCellValueFactory(new PropertyValueFactory<>("availability"));

        loadCars();
    }

    private void loadCars() {
        carData.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            String query = "SELECT carID, carmodel, cartype, colour, price_per_day, Availability FROM cars ORDER BY carID";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                carData.add(new Car(
                        rs.getInt("carID"),
                        rs.getString("carmodel"),
                        rs.getString("cartype"),
                        rs.getString("colour"),
                        rs.getDouble("price_per_day"),
                        rs.getString("Availability")
                ));
            }
            carsTable.setItems(carData);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load cars: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCar(ActionEvent event) {
        Car selected = carsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a car from the table first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete car: " + selected.getModel() + " (ID: " + selected.getCarId() + ")?");
        confirm.showAndWait();

        if (confirm.getResult() == javafx.scene.control.ButtonType.OK) {
            try (Connection conn = DBConnection.getConnection()) {
                String query = "DELETE FROM cars WHERE carID = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, selected.getCarId());
                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Car deleted successfully!");
                    loadCars();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Car not found.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
            }
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

    private void setBackground() {
        String path = "/com/example/carrental/pics/llg.png";
        try (java.io.InputStream is = getClass().getResourceAsStream(path)) {
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
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            rootPane.setBackground(new Background(bg));
        } catch (Exception e) {
            rootPane.setStyle("-fx-background-color: #111111;");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Car Rental System");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Car {
        private final int carId;
        private final String model;
        private final String type;
        private final String colour;
        private final double price;
        private final String availability;

        public Car(int carId, String model, String type, String colour, double price, String availability) {
            this.carId = carId;
            this.model = model;
            this.type = type;
            this.colour = colour;
            this.price = price;
            this.availability = availability;
        }

        public int getCarId() { return carId; }
        public String getModel() { return model; }
        public String getType() { return type; }
        public String getColour() { return colour; }
        public double getPrice() { return price; }
        public String getAvailability() { return availability; }
    }
}
