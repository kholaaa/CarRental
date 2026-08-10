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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class bookcarController implements Initializable {

    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField;
    @FXML private AnchorPane rootPane;
    @FXML private VBox bookingForm;
    @FXML private VBox selectedCarBox;
    @FXML private ImageView selectedCarImage;
    @FXML private Label selectedCarName;
    @FXML private Label selectedCarDetails;
    @FXML private Label selectedCarPrice;
    @FXML private Label placeholderLabel;
    @FXML private TextField carIdField;
    @FXML private DatePicker entryDatePicker;
    @FXML private DatePicker returnDatePicker;
    @FXML private Label costPreview;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private VBox cardDetailsBox;
    @FXML private TextField cardNumberField;
    @FXML private TextField cardExpiryField;
    @FXML private TextField cardCvvField;

    private ObservableList<Car> carData = FXCollections.observableArrayList();
    private Car selectedCar = null;

    public static int pendingCarId = -1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadBackgroundImage();
        FloatingChatButton.install(rootPane);

        paymentMethodCombo.setItems(FXCollections.observableArrayList(
                "Credit Card", "Debit Card", "JazzCash", "Easypaisa", "Cash on Delivery"
        ));
        paymentMethodCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCard = "Credit Card".equals(newVal) || "Debit Card".equals(newVal);
            cardDetailsBox.setVisible(isCard);
            cardDetailsBox.setManaged(isCard);
        });

        loadAvailableCars();
        renderCards(carData);

        if (pendingCarId != -1) {
            for (Car car : carData) {
                if (car.getCarId() == pendingCarId) {
                    selectCar(car);
                    break;
                }
            }
            pendingCarId = -1;
        }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            ObservableList<Car> filtered = FXCollections.observableArrayList(
                    SmartSearch.rank(newVal, carData));
            renderCards(filtered);
        });

        entryDatePicker.valueProperty().addListener((obs, old, val) -> updateCostPreview());
        returnDatePicker.valueProperty().addListener((obs, old, val) -> updateCostPreview());
    }

    private void renderCards(ObservableList<Car> cars) {
        cardsContainer.getChildren().clear();
        for (Car car : cars) {
            cardsContainer.getChildren().add(createCarCard(car));
        }
    }

    private VBox createCarCard(Car car) {
        VBox card = new VBox(8);
        card.getStyleClass().add("car-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(210);
        card.setPrefHeight(220);
        card.setPadding(new Insets(0, 0, 10, 0));

        if (selectedCar != null && selectedCar.getCarId() == car.getCarId()) {
            card.setStyle(card.getStyle() + "-fx-background-color: rgba(255,183,77,0.20); -fx-border-color: rgba(255,183,77,0.50);");
        }

        ImageView imageView = new ImageView();
        imageView.setFitWidth(190);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setImage(loadCarImage(car.getModel()));

        Label nameLabel = new Label(car.getModel());
        nameLabel.getStyleClass().add("car-card-name");

        Label detailsLabel = new Label(car.getType() + "  |  PKR " + String.format("%,.0f", car.getPrice()) + "/day");
        detailsLabel.getStyleClass().add("car-card-details");

        Label statusLabel = new Label(car.getStatus());
        if ("Available".equals(car.getStatus())) {
            statusLabel.getStyleClass().add("car-card-status-available");
        } else {
            statusLabel.getStyleClass().add("car-card-status-booked");
        }

        HBox statusRow = new HBox(statusLabel);
        statusRow.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imageView, nameLabel, detailsLabel, statusRow);

        card.setOnMouseClicked(e -> selectCar(car));

        return card;
    }

    private void selectCar(Car car) {
        if ("Booked".equals(car.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "This car is currently booked.");
            return;
        }

        selectedCar = car;
        carIdField.setText(String.valueOf(car.getCarId()));

        selectedCarName.setText(car.getModel());
        selectedCarDetails.setText(car.getType());
        selectedCarPrice.setText("PKR " + String.format("%,.0f", car.getPrice()) + " / day");
        selectedCarImage.setImage(loadCarImage(car.getModel()));

        selectedCarBox.setVisible(true);
        placeholderLabel.setVisible(false);

        renderCards(carData);
        updateCostPreview();
    }

    private void updateCostPreview() {
        if (selectedCar == null || entryDatePicker.getValue() == null || returnDatePicker.getValue() == null) {
            costPreview.setText("");
            return;
        }
        LocalDate entry = entryDatePicker.getValue();
        LocalDate ret = returnDatePicker.getValue();
        if (ret.isAfter(entry)) {
            long days = ChronoUnit.DAYS.between(entry, ret);
            double total = days * selectedCar.getPrice();
            costPreview.setText(days + " days  x  PKR " + String.format("%,.0f", selectedCar.getPrice()) + "  =  PKR " + String.format("%,.0f", total));
        } else {
            costPreview.setText("");
        }
    }

    private void loadBackgroundImage() {
        String imagePath = "/com/example/carrental/pics/llg.png";
        try (InputStream stream = getClass().getResourceAsStream(imagePath)) {
            if (stream == null) {
                rootPane.setStyle("-fx-background-color: #0f171e;");
                return;
            }
            Image bgImage = new Image(stream);
            BackgroundImage backgroundImage = new BackgroundImage(
                    bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
            );
            rootPane.setBackground(new Background(backgroundImage));
        } catch (Exception e) {
            e.printStackTrace();
            rootPane.setStyle("-fx-background-color: #0f171e;");
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
            String query = """
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

    @FXML
    private void handleBookCar(ActionEvent event) {
        if (selectedCar == null) {
            showAlert(Alert.AlertType.ERROR, "Please select a car first.");
            return;
        }

        int carId = selectedCar.getCarId();

        LocalDate entryDate = entryDatePicker.getValue();
        LocalDate returnDate = returnDatePicker.getValue();

        if (entryDate == null || returnDate == null) {
            showAlert(Alert.AlertType.ERROR, "Please select both start and return dates.");
            return;
        }

        if (returnDate.isBefore(entryDate) || returnDate.isEqual(entryDate)) {
            showAlert(Alert.AlertType.ERROR, "Return date must be after start date.");
            return;
        }

        long days = ChronoUnit.DAYS.between(entryDate, returnDate);
        if (days <= 0) {
            showAlert(Alert.AlertType.ERROR, "Minimum rental period is 1 day.");
            return;
        }

        int customerId = Session.getUserId();
        if (customerId == -1) {
            showAlert(Alert.AlertType.ERROR, "You must be logged in to book a car.");
            return;
        }

        String paymentMethod = paymentMethodCombo.getValue();
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please select a payment method.");
            return;
        }

        if ("Credit Card".equals(paymentMethod) || "Debit Card".equals(paymentMethod)) {
            if (!validateCardPayment()) {
                return;
            }
        }

        try (Connection conn = DBConnection.getConnection()) {
            String carQuery = "SELECT price_per_day FROM cars WHERE carID = ? AND Availability = 'Yes'";
            PreparedStatement carStmt = conn.prepareStatement(carQuery);
            carStmt.setInt(1, carId);
            ResultSet carRs = carStmt.executeQuery();

            if (!carRs.next()) {
                showAlert(Alert.AlertType.ERROR, "Car not found or currently unavailable.");
                return;
            }

            double pricePerDay = carRs.getDouble("price_per_day");
            double totalCost = days * pricePerDay;

            String overlapQuery =
                    "SELECT COUNT(*) FROM bookcar " +
                            "WHERE carID = ? AND " +
                            "(entrydate <= ? AND returndate >= ?)";
            PreparedStatement overlapStmt = conn.prepareStatement(overlapQuery);
            overlapStmt.setInt(1, carId);
            overlapStmt.setDate(2, Date.valueOf(returnDate));
            overlapStmt.setDate(3, Date.valueOf(entryDate));
            ResultSet overlapRs = overlapStmt.executeQuery();
            overlapRs.next();

            if (overlapRs.getInt(1) > 0) {
                showAlert(Alert.AlertType.ERROR, "This car is already booked for the selected dates.");
                return;
            }

            String insertQuery =
                    "INSERT INTO bookcar (carID, customerID, entrydate, returndate, total_cost) " +
                            "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, carId);
            insertStmt.setInt(2, customerId);
            insertStmt.setDate(3, Date.valueOf(entryDate));
            insertStmt.setDate(4, Date.valueOf(returnDate));
            insertStmt.setDouble(5, totalCost);
            insertStmt.executeUpdate();

            String updateAvailability = "UPDATE cars SET Availability = 'No' WHERE carID = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateAvailability);
            updateStmt.setInt(1, carId);
            updateStmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION,
                    "Booking successful!\n" +
                            "Car: " + selectedCar.getModel() + "\n" +
                            "Rental Days: " + days + "\n" +
                            "Total Cost: " + String.format("%,.0f", totalCost) + " PKR\n" +
                            "Payment Method: " + paymentMethod);

            selectedCar = null;
            carIdField.clear();
            entryDatePicker.setValue(null);
            returnDatePicker.setValue(null);
            selectedCarBox.setVisible(false);
            placeholderLabel.setVisible(true);
            costPreview.setText("");

            loadAvailableCars();
            renderCards(carData);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
            e.printStackTrace();
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

    private boolean validateCardPayment() {
        String cardNumber = cardNumberField.getText().replaceAll("\\s", "");
        String expiry = cardExpiryField.getText().trim();
        String cvv = cardCvvField.getText().trim();

        if (cardNumber.isEmpty() || cardNumber.length() != 16 || !cardNumber.matches("\\d{16}")) {
            showAlert(Alert.AlertType.ERROR, "Please enter a valid 16-digit card number.");
            return false;
        }
        if (expiry.isEmpty() || !expiry.matches("\\d{2}/\\d{2}")) {
            showAlert(Alert.AlertType.ERROR, "Please enter card expiry as MM/YY.");
            return false;
        }
        if (cvv.isEmpty() || !cvv.matches("\\d{3,4}")) {
            showAlert(Alert.AlertType.ERROR, "Please enter a valid CVV (3-4 digits).");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Car Rental System");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}
