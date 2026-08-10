package com.example.carrental.ai;

import com.example.carrental.DBConnection;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Suggests a price for a car based on the average market price for its type,
 * historic + recent demand, the rental day of the week, and seasonality.
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

    /** Suggests a price as of today (kept for backward compatibility). */
    public static PriceSuggestion suggestPrice(String carType) {
        return suggestPrice(carType, LocalDate.now());
    }

    /** Suggests a price for a specific rental date (dynamic pricing / fare predictor). */
    public static PriceSuggestion suggestPrice(String carType, LocalDate rentalDate) {
        if (carType == null || carType.trim().isEmpty()) {
            return new PriceSuggestion(0, "Enter a car type first.");
        }
        if (rentalDate == null) rentalDate = LocalDate.now();

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

            double price = basePrice;
            List<String> factors = new ArrayList<>();

            // 1. Historic demand for this car type
            long bookings = countBookings(conn, carType, null);
            if (bookings >= 5) {
                price *= 1.10;
                factors.add(String.format("high demand (%d past bookings): +10%%", bookings));
            } else if (bookings == 0) {
                price *= 0.90;
                factors.add("no booking history yet: -10% (attract first bookings)");
            } else {
                factors.add(String.format("moderate demand (%d past bookings)", bookings));
            }

            // 2. Recent demand (last 30 days) — hot streak
            long recent = countBookings(conn, carType, LocalDate.now().minusDays(30));
            if (recent >= 2) {
                price *= 1.05;
                factors.add(String.format("hot in the last 30 days (%d bookings): +5%%", recent));
            }

            // 3. Day of week
            DayOfWeek dow = rentalDate.getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                price *= 1.08;
                factors.add("weekend demand: +8%");
            } else if (dow == DayOfWeek.FRIDAY) {
                price *= 1.04;
                factors.add("Friday (pre-weekend travel): +4%");
            } else {
                price *= 0.96;
                factors.add("weekday (Mon-Thu): -4%");
            }

            // 4. Seasonality (peak travel months)
            int month = rentalDate.getMonthValue();
            if (month == 12 || month == 5 || month == 6 || month == 7 || month == 8) {
                price *= 1.06;
                factors.add("peak season: +6%");
            } else if (month == 1 || month == 2) {
                price *= 0.95;
                factors.add("off-peak season: -5%");
            }

            double rounded = Math.round(price * 100.0) / 100.0;
            String reason = String.format(
                    "Market avg %.0f PKR/day for %s on %s (%s). %s",
                    basePrice, carType, rentalDate, dow,
                    String.join("; ", factors));

            return new PriceSuggestion(rounded, reason);

        } catch (SQLException e) {
            e.printStackTrace();
            return new PriceSuggestion(0, "Could not calculate suggestion (database error).");
        }
    }

    private static long countBookings(Connection conn, String carType, LocalDate since) throws SQLException {
        String sql = "SELECT COUNT(*) AS bookings FROM bookcar b JOIN cars c ON b.carID = c.carID " +
                "WHERE c.cartype = ?";
        if (since != null) sql += " AND b.booking_date >= ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, carType);
        if (since != null) ps.setDate(2, java.sql.Date.valueOf(since));
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getLong("bookings") : 0;
    }
}