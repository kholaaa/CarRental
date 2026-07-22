package com.example.carrental.ai;

import com.example.carrental.DBConnection;

import java.sql.*;

/**
 * Suggests a price for a car based on the average market price for its type
 * and how frequently that type has historically been booked (demand).
 */
public class PricingAdvisor {

    public static class PriceSuggestion {
        public final double suggestedPrice;
        public final String reason;

        public PriceSuggestion(double suggestedPrice, String reason) {
            this.suggestedPrice = suggestedPrice;
            this.reason = reason;
        }
    }

    public static PriceSuggestion suggestPrice(String carType) {
        if (carType == null || carType.trim().isEmpty()) {
            return new PriceSuggestion(0, "Enter a car type first.");
        }

        try (Connection conn = DBConnection.getConnection()) {

            // Average existing price for this type
            String avgQuery = "SELECT AVG(price_per_day) AS avgPrice, COUNT(*) AS cnt " +
                    "FROM cars WHERE cartype = ?";
            PreparedStatement avgStmt = conn.prepareStatement(avgQuery);
            avgStmt.setString(1, carType);
            ResultSet avgRs = avgStmt.executeQuery();

            double basePrice = 3000; // fallback default when no matching cars exist yet
            if (avgRs.next() && avgRs.getInt("cnt") > 0) {
                basePrice = avgRs.getDouble("avgPrice");
            }

            // Demand: how many bookings this car type has had historically
            String demandQuery = """
                SELECT COUNT(*) AS bookings
                FROM bookcar b
                JOIN cars c ON b.carID = c.carID
                WHERE c.cartype = ?
            """;
            PreparedStatement demandStmt = conn.prepareStatement(demandQuery);
            demandStmt.setString(1, carType);
            ResultSet demandRs = demandStmt.executeQuery();
            int bookings = demandRs.next() ? demandRs.getInt("bookings") : 0;

            double adjustedPrice;
            String reason;

            if (bookings >= 5) {
                adjustedPrice = basePrice * 1.10;
                reason = String.format("High demand (%d past bookings for %s) — suggest 10%% above average.", bookings, carType);
            } else if (bookings == 0) {
                adjustedPrice = basePrice * 0.90;
                reason = String.format("No booking history for %s — suggest 10%% discount to attract first bookings.", carType);
            } else {
                adjustedPrice = basePrice;
                reason = String.format("Moderate demand (%d past bookings) — suggest average market price.", bookings);
            }

            return new PriceSuggestion(Math.round(adjustedPrice * 100.0) / 100.0, reason);

        } catch (SQLException e) {
            e.printStackTrace();
            return new PriceSuggestion(0, "Could not calculate suggestion (database error).");
        }
    }
}