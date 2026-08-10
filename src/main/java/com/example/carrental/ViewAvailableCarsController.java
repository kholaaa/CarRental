package com.example.carrental;

import com.example.carrental.ai.SmartSearch;
import com.example.carrental.chat.FloatingChatButton;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    @FXML private FlowPane cardsContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField searchField;
    @FXML private AnchorPane rootPane;

    private ObservableList<Car> carData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setDarkBackground(rootPane, "llg.png");
        FloatingChatButton.install(rootPane);

        loadAvailableCars();
        renderCards(carData);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            ObservableList<Car> filtered = FXCollections.observableArrayList(
                    SmartSearch.rank(newVal, carData));
            renderCards(filtered);
        });
    }

    private void renderCards(ObservableList<Car> cars) {
        cardsContainer.getChildren().clear();
        for (Car car : cars) {
            cardsContainer.getChildren().add(createCarCard(car));
        }
    }

    private VBox createCarCard(Car car) {
        VBox card = new VBox(10);
        card.getStyleClass().add("car-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(240);
        card.setPrefHeight(280);
        card.setPadding(new Insets(0, 0, 14, 0));

        // Car image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        imageView.setImage(loadCarImage(car.getModel()));

        // Car name
        Label nameLabel = new Label(car.getModel());
        nameLabel.getStyleClass().add("car-card-name");

        // Type + Price row
        Label detailsLabel = new Label(car.getType() + "  |  PKR " + String.format("%,.0f", car.getPrice()) + "/day");
        detailsLabel.getStyleClass().add("car-card-details");

        // Status badge
        Label statusLabel = new Label(car.getStatus());
        if ("Available".equals(car.getStatus())) {
            statusLabel.getStyleClass().add("car-card-status-available");
        } else {
            statusLabel.getStyleClass().add("car-card-status-booked");
        }

        HBox statusRow = new HBox(statusLabel);
        statusRow.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imageView, nameLabel, detailsLabel, statusRow);

        if ("Available".equals(car.getStatus()) && !Session.isAdmin()) {
            card.setOnMouseClicked(e -> {
                try {
                    bookcarController.pendingCarId = car.getCarId();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/bookcar.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) rootPane.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.centerOnScreen();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }

        return card;
    }

    private void setDarkBackground(AnchorPane rootPane, String imageName) {
        String fullPath = "/com/example/carrental/pics/" + imageName;
        try (InputStream stream = getClass().getResourceAsStream(fullPath)) {
            if (stream == null) {
                rootPane.setStyle("-fx-background-color: #111111;");
                return;
            }
            Image bgImage = new Image(stream);
            javafx.scene.layout.BackgroundImage backgroundImage = new javafx.scene.layout.BackgroundImage(
                    bgImage,
                    javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                    javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                    javafx.scene.layout.BackgroundPosition.CENTER,
                    new javafx.scene.layout.BackgroundSize(
                            javafx.scene.layout.BackgroundSize.AUTO,
                            javafx.scene.layout.BackgroundSize.AUTO,
                            true, true, true, true)
            );
            rootPane.setBackground(new javafx.scene.layout.Background(backgroundImage));
        } catch (Exception e) {
            e.printStackTrace();
            rootPane.setStyle("-fx-background-color: #111111;");
        }
    }

    private Image loadCarImage(String model) {
        String[] extensions = {"png", "jpg", "jpeg"};
        for (String ext : extensions) {
            String path = "/com/example/carrental/pics/cars/" + model.toLowerCase() + "." + ext;
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is != null) return new Image(is);
            } catch (Exception ignored) {}
        }
        String fallback = "/com/example/carrental/pics/cars/default.png";
        try (InputStream is = getClass().getResourceAsStream(fallback)) {
            if (is != null) return new Image(is);
        } catch (Exception ignored) {}
        return null;
    }

    private void loadAvailableCars() {
        carData.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String query =  """
                SELECT 
                    c.carID, c.carmodel, c.cartype, c.price_per_day,
                    CASE 
                        WHEN EXISTS (
                            SELECT 1 FROM bookcar b 
                            WHERE b.carID = c.carID 
                            AND (b.returned IS NULL OR b.returned = 0)
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Car implements SmartSearch.Priced {
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

        @Override
        public String getSearchableText() {
            return model + " " + type;
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
}
