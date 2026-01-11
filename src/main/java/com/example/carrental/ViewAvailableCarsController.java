package com.example.carrental;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class ViewAvailableCarsController implements Initializable {

    @FXML private TableView<Car> carsTable;
    @FXML private TableColumn<Car, Integer> idCol;
    @FXML private TableColumn<Car, String> modelCol;
    @FXML private TableColumn<Car, String> typeCol;
    @FXML private TableColumn<Car, Double> priceCol;
    @FXML private TableColumn<Car, String> statusCol; // NEW: Availability status

    @FXML private AnchorPane rootPane;

    private ObservableList<Car> carData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set background
        setDarkBackground(rootPane, "img.png");

        // Set up table columns
        idCol.setCellValueFactory(new PropertyValueFactory<>("carId"));
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status")); // NEW

        loadAvailableCars();
    }

    private void setDarkBackground(AnchorPane rootPane, String imageName) {
        String fullPath = "/com/example/carrental/pics/" + imageName;
        try (InputStream stream = getClass().getResourceAsStream(fullPath)) {
            if (stream == null) {
                System.err.println("Background not found: " + fullPath);
                rootPane.setStyle("-fx-background-color: #111111;");
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
            rootPane.setStyle("-fx-background-color: #111111;");
        }
    }

    private void loadAvailableCars() {
        carData.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Query: All cars with real-time status
            String query = """
                SELECT 
                    c.carID, c.carmodel, c.cartype, c.price_per_day,
                    CASE 
                        WHEN EXISTS (
                            SELECT 1 FROM bookcar b 
                            WHERE b.carID = c.carID 
                            AND b.returndate >= CURDATE()
                        ) THEN 'Booked'
                        ELSE 'Available'
                    END AS status
                FROM cars c
                ORDER BY c.carID
            """;

            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                carData.add(new Car(
                        rs.getInt("carID"),
                        rs.getString("carmodel"),
                        rs.getString("cartype"),
                        rs.getDouble("price_per_day"),
                        rs.getString("status")
                ));
            }

            carsTable.setItems(carData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Updated Car class
    public static class Car {
        private final int carId;
        private final String model;
        private final String type;
        private final double price;
        private final String status;

        public Car(int carId, String model, String type, double price, String status) {
            this.carId = carId;
            this.model = model;
            this.type = type;
            this.price = price;
            this.status = status;
        }

        public int getCarId() { return carId; }
        public String getModel() { return model; }
        public String getType() { return type; }
        public double getPrice() { return price; }
        public String getStatus() { return status; }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/com/example/carrental/Dashboard.fxml"));
        Scene scene = new Scene(dashboardRoot);
        stage.setScene(scene);
        stage.centerOnScreen();
    }
}