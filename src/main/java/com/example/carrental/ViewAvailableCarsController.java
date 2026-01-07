package com.example.carrental;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ViewAvailableCarsController {

    @FXML private ImageView backgroundImage;
    @FXML private TableView<Car> carsTable;
    @FXML private TableColumn<Car, Integer> carIdCol;
    @FXML private TableColumn<Car, String> modelCol;
    @FXML private TableColumn<Car, String> typeCol;
    @FXML private TableColumn<Car, String> colorCol;
    @FXML private TableColumn<Car, Integer> priceCol;
    @FXML private TableColumn<Car, String> availabilityCol;

    @FXML
    public void initialize() {
        backgroundImage.setImage(new Image(""));

        carIdCol.setCellValueFactory(data -> data.getValue().carIdProperty().asObject());
        modelCol.setCellValueFactory(data -> data.getValue().modelProperty());
        typeCol.setCellValueFactory(data -> data.getValue().typeProperty());
        colorCol.setCellValueFactory(data -> data.getValue().colorProperty());
        priceCol.setCellValueFactory(data -> data.getValue().priceProperty().asObject());
        availabilityCol.setCellValueFactory(data -> data.getValue().availabilityProperty());

        loadCars();
    }

    private void loadCars() {
        ObservableList<Car> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT carid, carmodel, cartype, colour, price_per_day, availability FROM cars")) {
            while (rs.next()) {
                list.add(new Car(
                        rs.getInt("carid"),
                        rs.getString("carmodel"),
                        rs.getString("cartype"),
                        rs.getString("colour"),
                        rs.getInt("price_per_day"),
                        rs.getString("availability")
                ));
            }
            carsTable.setItems(list);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) carsTable.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/com/example/carrental/dashboard.fxml"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}