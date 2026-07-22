package com.example.carrental.ai;

import com.example.carrental.DBConnection;

import java.sql.*;
import java.util.*;

/**
 * Content-based car recommendation engine.
 *
 * Looks at a customer's past bookings (bookcar + cars) to learn which
 * car types and price ranges they favour, then scores currently available
 * cars against that profile. Falls back to global popularity for new
 * customers with no rental history.
 */
public class RecommendationEngine {

    public static class ScoredCar {
        public final int carId;
        public final String model;
        public final String type;
        public final double pricePerDay;
        public final double score; // 0..1, higher = more relevant

        public ScoredCar(int carId, String model, String type, double pricePerDay, double score) {
            this.carId = carId;
            this.model = model;
            this.type = type;
            this.pricePerDay = pricePerDay;
            this.score = score;
        }
    }

    /** Returns available cars ranked by relevance to this customer, best first. */
    public static List<ScoredCar> recommend(int customerId, int limit) {
        Map<String, Integer> typeFrequency = new HashMap<>();
        List<Double> pastPrices = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Customer's rental history: which types & prices they've booked
            String historyQuery = """
                SELECT c.cartype, c.price_per_day
                FROM bookcar b
                JOIN cars c ON b.carID = c.carID
                WHERE b.customerID = ?
            """;
            PreparedStatement histStmt = conn.prepareStatement(historyQuery);
            histStmt.setInt(1, customerId);
            ResultSet histRs = histStmt.executeQuery();

            while (histRs.next()) {
                typeFrequency.merge(histRs.getString("cartype"), 1, Integer::sum);
                pastPrices.add(histRs.getDouble("price_per_day"));
            }

            double avgPastPrice = pastPrices.isEmpty() ? -1 :
                    pastPrices.stream().mapToDouble(Double::doubleValue).average().orElse(-1);

            // 2. New customer with no history -> fall back to overall popular types
            if (typeFrequency.isEmpty()) {
                typeFrequency = fetchGloballyPopularTypes(conn);
            }

            // 3. Score every currently available car
            String availableQuery = """
                SELECT carID, carmodel, cartype, price_per_day
                FROM cars
                WHERE Availability = 'Yes'
            """;
            Statement availStmt = conn.createStatement();
            ResultSet availRs = availStmt.executeQuery(availableQuery);

            int maxTypeFreq = typeFrequency.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            List<ScoredCar> scored = new ArrayList<>();

            while (availRs.next()) {
                String type = availRs.getString("cartype");
                double price = availRs.getDouble("price_per_day");

                double typeScore = typeFrequency.getOrDefault(type, 0) / (double) maxTypeFreq; // 0..1

                double priceScore;
                if (avgPastPrice > 0) {
                    double diff = Math.abs(price - avgPastPrice);
                    priceScore = 1.0 / (1.0 + diff / Math.max(avgPastPrice, 1)); // closer price -> higher score
                } else {
                    priceScore = 0.5; // neutral when we don't know their budget
                }

                double totalScore = (0.7 * typeScore) + (0.3 * priceScore);

                scored.add(new ScoredCar(
                        availRs.getInt("carID"),
                        availRs.getString("carmodel"),
                        type,
                        price,
                        totalScore
                ));
            }

            scored.sort((a, b) -> Double.compare(b.score, a.score));
            return scored.size() > limit ? scored.subList(0, limit) : scored;

        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static Map<String, Integer> fetchGloballyPopularTypes(Connection conn) throws SQLException {
        Map<String, Integer> freq = new HashMap<>();
        String query = """
            SELECT c.cartype, COUNT(*) AS cnt
            FROM bookcar b
            JOIN cars c ON b.carID = c.carID
            GROUP BY c.cartype
        """;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            freq.put(rs.getString("cartype"), rs.getInt("cnt"));
        }
        return freq;
    }
}
