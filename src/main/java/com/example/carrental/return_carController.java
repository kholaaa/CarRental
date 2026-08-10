package com.example.carrental;

import javafx.event.ActionEvent;
import com.example.carrental.chat.FloatingChatButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class return_carController {

    @FXML private TextField carIdField;
    @FXML private TextField fuelLevelField;
    @FXML private TextArea conditionField;
    @FXML private AnchorPane rootPane;

    private static final String CHECK_BOOKING_SQL = """
        SELECT bookcarID, returndate
        FROM bookcar
        WHERE carID = ?
          AND customerID = ?
          AND entrydate <= CURDATE()
          AND returndate >= CURDATE()
          AND (returned IS NULL OR returned = 0)
    """;

    @FXML
    public void initialize() {
        setBackground();
        FloatingChatButton.install(rootPane);
    }

    @FXML
    private void handleReturnCar(ActionEvent event) {
        try {
            int carId = Integer.parseInt(carIdField.getText().trim());
            int fuelLevel = Integer.parseInt(fuelLevelField.getText().trim());
            String condition = conditionField.getText().trim();

            if (fuelLevel < 0 || fuelLevel > 100) {
                showAlert(Alert.AlertType.ERROR, "Fuel level must be between 0 and 100.");
                return;
            }

            if (condition.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Please describe the car condition.");
                return;
            }

            int customerId = Session.getUserId();
            if (customerId == -1) {
                showAlert(Alert.AlertType.ERROR, "You must be logged in to return a car.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                try {
                    // 1. Check active (not returned) booking
                    PreparedStatement checkStmt = conn.prepareStatement(CHECK_BOOKING_SQL);
                    checkStmt.setInt(1, carId);
                    checkStmt.setInt(2, customerId);
                    ResultSet rs = checkStmt.executeQuery();

                    if (!rs.next()) {
                        showAlert(Alert.AlertType.ERROR, "You do not have an active booking for this car.");
                        conn.rollback();
                        return;
                    }
                    Date bookedReturnDate = rs.getDate("returndate");

                    // 2. Insert return record
                    String insertReturn = """
                        INSERT INTO returncar (carID, customerID, returndate, fuellevel, carcondition)
                        VALUES (?, ?, CURDATE(), ?, ?)
                    """;
                    PreparedStatement insertStmt = conn.prepareStatement(insertReturn);
                    insertStmt.setInt(1, carId);
                    insertStmt.setInt(2, customerId);
                    insertStmt.setInt(3, fuelLevel);
                    insertStmt.setString(4, condition);
                    insertStmt.executeUpdate();

                    // 3. Mark booking as returned
                    String markBookingComplete = """
                        UPDATE bookcar 
                        SET returned = 1, actual_return_date = CURDATE()
                        WHERE carID = ? AND customerID = ? AND returndate >= CURDATE()
                    """;
                    PreparedStatement markStmt = conn.prepareStatement(markBookingComplete);
                    markStmt.setInt(1, carId);
                    markStmt.setInt(2, customerId);
                    markStmt.executeUpdate();

                    // 4. (Optional safety) Mark car available in cars table
                    String updateCar = "UPDATE cars SET Availability = 'Yes' WHERE carID = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateCar);
                    updateStmt.setInt(1, carId);
                    updateStmt.executeUpdate();

                    conn.commit();

                    showAlert(Alert.AlertType.INFORMATION, "Car returned successfully!\nCar is now available.");

                    showReturnRiskAssessment(fuelLevel, condition, bookedReturnDate);

                    carIdField.clear();
                    fuelLevelField.clear();
                    conditionField.clear();

                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Car ID and Fuel Level must be valid numbers.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
        }
    }

    private void setBackground() {
        String path = "/com/example/carrental/pics/llg.png";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                rootPane.setStyle("-fx-background-color: #222;");
                return;
            }
            Image img = new Image(is);
            BackgroundImage bg = new BackgroundImage(
                    img,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            rootPane.setBackground(new Background(bg));
        } catch (Exception e) {
            rootPane.setStyle("-fx-background-color: #222;");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/carrental/Dashboard.fxml"));
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Unable to load Dashboard.");
        }
    }

    private void showReturnRiskAssessment(int fuelLevel, String condition, Date bookedReturnDate) {
        RiskAssessment risk = assessReturnRisk(fuelLevel, condition, bookedReturnDate);
        if (risk.score < 40) return;

        StringBuilder msg = new StringBuilder("Return Risk Assessment: ")
                .append(risk.level).append(" (").append(risk.score).append("/100)\n\n");
        for (String reason : risk.reasons) {
            msg.append("- ").append(reason).append("\n");
        }
        msg.append("\nAn inspection is recommended before releasing this car to the next customer.");

        showAlert(risk.score >= 70 ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION, msg.toString());
    }

    private RiskAssessment assessReturnRisk(int fuelLevel, String condition, Date bookedReturnDate) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (fuelLevel < 25) {
            score += 30;
            reasons.add("Fuel critically low (" + fuelLevel + "%) — +30");
        } else if (fuelLevel < 50) {
            score += 15;
            reasons.add("Fuel low (" + fuelLevel + "%) — +15");
        }

        String cond = condition.toLowerCase(Locale.ROOT);
        String[] damageKeywords = {"damage", "scratch", "dent", "broken", "crack", "fault",
                "issue", "leak", "smoke", "burn", "not working", "failed", "stain"};
        for (String kw : damageKeywords) {
            if (cond.contains(kw)) {
                score += 40;
                reasons.add("Damage indicator found: \"" + kw + "\" — +40");
                break;
            }
        }

        if (bookedReturnDate != null) {
            LocalDate actual = LocalDate.now();
            if (actual.isAfter(bookedReturnDate.toLocalDate())) {
                long daysLate = ChronoUnit.DAYS.between(bookedReturnDate.toLocalDate(), actual);
                int penalty = (int) Math.min(20, daysLate * 5);
                score += penalty;
                reasons.add("Late return (" + daysLate + " day(s) overdue) — +" + penalty);
            }
        }

        if (condition.trim().length() > 120) {
            score += 10;
            reasons.add("Unusually long condition note — +10");
        }

        String level = score >= 70 ? "HIGH" : score >= 40 ? "MEDIUM" : "LOW";
        return new RiskAssessment(level, score, reasons);
    }

    private static class RiskAssessment {
        final String level;
        final int score;
        final List<String> reasons;

        RiskAssessment(String level, int score, List<String> reasons) {
            this.level = level;
            this.score = score;
            this.reasons = reasons;
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Car Rental System");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}