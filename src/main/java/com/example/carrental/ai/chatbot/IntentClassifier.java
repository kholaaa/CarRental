package com.example.carrental.ai.chatbot;

import com.example.carrental.DBConnection;
import com.example.carrental.Session;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class IntentClassifier {

    private String lastIntent = "none";
    private String lastExtractedType = null;

    public String respond(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "I didn't quite catch that. Could you rephrase?";
        }

        String msg = userMessage.toLowerCase(Locale.ROOT).trim();
        String[] words = msg.split("\\s+");

        // Greetings
        if (matchesAny(msg, "hello", "hi ", "hi!", "hey", "howdy", "greetings", "yo ", "yo!")) {
            String name = getCustomerName();
            lastIntent = "greeting";
            return name != null
                    ? "Hey " + name + "! How can I help you today? You can ask about cars, prices, bookings, or anything else."
                    : "Hey there! I'm your car rental assistant. Ask me anything about cars, prices, or bookings!";
        }

        // Farewells
        if (matchesAny(msg, "bye", "goodbye", "see you", "take care", "later", "see ya", "cya")) {
            lastIntent = "farewell";
            return "Goodbye! Drive safe and come back anytime you need a car!";
        }

        // Help
        if (matchesAny(msg, "help", "what can you do", "commands", "options", "menu", "features")) {
            lastIntent = "help";
            return buildHelpMessage();
        }

        // Who are you
        if (matchesAny(msg, "who are you", "your name", "what are you", "tell me about yourself")) {
            lastIntent = "identity";
            return "I'm your Car Rental AI Assistant! I can help you with:\n"
                    + "- Checking available cars\n"
                    + "- Looking up prices by car type\n"
                    + "- Viewing your active bookings\n"
                    + "- Estimating rental costs\n"
                    + "- Checking if a car is available on specific dates\n"
                    + "- Recommending cars within your budget\n\n"
                    + "Just ask me anything in natural language!";
        }

        // Total fleet stats
        if (matchesAny(msg, "how many cars", "total cars", "fleet size", "car count", "number of cars")) {
            lastIntent = "fleet_stats";
            return getFleetStats();
        }

        // Cheapest cars
        if (matchesAny(msg, "cheapest", "lowest price", "budget car", "affordable", "most affordable", "least expensive")) {
            lastIntent = "cheapest";
            String budgetStr = extractNumber(msg);
            if (budgetStr != null) {
                return getCarsUnderBudget(Double.parseDouble(budgetStr));
            }
            return getCheapestCars();
        }

        // Most expensive
        if (matchesAny(msg, "most expensive", "luxury", "premium", "highest price", "top price")) {
            lastIntent = "expensive";
            return getMostExpensiveCars();
        }

        // Car types / categories
        if (matchesAny(msg, "what types", "car types", "categories", "what kind", "what kinds", "types of car")) {
            lastIntent = "types";
            return getCarTypes();
        }

        // Available cars
        if (matchesAny(msg, "available car", "which cars", "show cars", "cars available",
                "what cars", "list cars", "car list", "all cars", "cars do you have",
                "what do you have", "inventory")) {
            lastIntent = "available_cars";
            return listAvailableCars();
        }

        // Specific car lookup (e.g., "toyota corolla", "civic details", "what about BMW")
        String specificCar = extractSpecificCar(msg);
        if (specificCar != null && !matchesAny(msg, "price", "cost", "how much")) {
            lastIntent = "specific_car";
            return getCarDetails(specificCar);
        }

        // Price / cost queries
        if (matchesAny(msg, "price", "cost", "how much", "rate", "pricing", "tariff", "fee")) {
            lastIntent = "price";
            String type = extractCarType(msg);
            if (type != null) {
                return priceForType(type);
            }
            String carName = extractSpecificCar(msg);
            if (carName != null) {
                return getPriceForCar(carName);
            }
            String budgetStr = extractNumber(msg);
            if (budgetStr != null) {
                return getCarsUnderBudget(Double.parseDouble(budgetStr));
            }
            return "What would you like to know the price of?\n"
                    + "You can say:\n"
                    + "- \"price of sedan\"\n"
                    + "- \"how much is a Toyota\"\n"
                    + "- \"cheapest car under 3000\"\n"
                    + "- \"show all prices\"";
        }

        // Estimate cost for a booking
        if (matchesAny(msg, "estimate", "calculate", "how much would", "total cost",
                "how much for", "what would it cost", "bill")) {
            lastIntent = "estimate";
            return estimateBookingCost(msg);
        }

        // Check availability on specific date
        if (matchesAny(msg, "is available", "available on", "free on", "booked on",
                "available for", "can i book", "available today")) {
            lastIntent = "availability_check";
            return checkAvailabilityOnDate(msg);
        }

        // My bookings
        if (matchesAny(msg, "my booking", "my rentals", "what did i book", "my reservation",
                "my car", "what have i booked", "current booking", "active booking")) {
            lastIntent = "my_bookings";
            return myActiveBookings();
        }

        // My booking history (including returned)
        if (matchesAny(msg, "booking history", "past bookings", "old bookings", "rental history",
                "previous rental", "returned cars")) {
            lastIntent = "booking_history";
            return myBookingHistory();
        }

        // Book / rent
        if (matchesAny(msg, "book a car", "book car", "how to book", "rent a car",
                "rent car", "i want to book", "i want to rent", "reserve")) {
            lastIntent = "how_to_book";
            return "To book a car:\n"
                    + "1. Go to Dashboard → Book a Car\n"
                    + "2. Enter the Car ID you want\n"
                    + "3. Pick your start and return dates\n"
                    + "4. Confirm!\n\n"
                    + "Tip: Ask me \"available cars\" to see what's ready to book.";
        }

        // Return car
        if (matchesAny(msg, "return a car", "return car", "how to return", "give back",
                "i want to return", "return my")) {
            lastIntent = "how_to_return";
            return "To return a car:\n"
                    + "1. Go to Dashboard → Return Vehicle\n"
                    + "2. Enter the Car ID\n"
                    + "3. Enter the current fuel level (0-100)\n"
                    + "4. Describe the car's condition\n"
                    + "5. Confirm!\n\n"
                    + "The car will become available for others immediately.";
        }

        // Recommend / suggest
        if (matchesAny(msg, "recommend", "suggest", "what should i", "which car should",
                "best car", "good car", "what do you suggest", "advice")) {
            lastIntent = "recommend";
            return getRecommendation(msg);
        }

        // Compare cars
        if (matchesAny(msg, "compare", "difference", "vs", "versus", "better", "which is better")) {
            lastIntent = "compare";
            return compareCars(msg);
        }

        // Best rated / popular
        if (matchesAny(msg, "popular", "most booked", "trending", "best selling", "top car")) {
            lastIntent = "popular";
            return getMostBookedCars();
        }

        // Thank you
        if (matchesAny(msg, "thank", "thanks", "appreciate", "great", "awesome", "perfect",
                "good job", "nice", "cool")) {
            lastIntent = "thanks";
            return pickRandom(
                    "You're welcome! Happy to help.",
                    "Glad I could help! Anything else?",
                    "No problem! Let me know if you need anything else.",
                    "Anytime! That's what I'm here for."
            );
        }

        // Yes/No follow-ups
        if (matchesAny(msg, "yes", "yeah", "sure", "ok", "okay", "yep")) {
            return handleYesFollowUp();
        }

        if (matchesAny(msg, "no", "nah", "nope", "not really")) {
            return handleNoFollowUp();
        }

        // Fallback with suggestion
        lastIntent = "unknown";
        return "I'm not sure I understand that. Here are some things you can ask me:\n"
                + "- \"What cars are available?\"\n"
                + "- \"Price of sedan\"\n"
                + "- \"My bookings\"\n"
                + "- \"Estimate cost for 3 days\"\n"
                + "- \"Recommend me a car\"\n"
                + "- \"Help\" for full list of commands";
    }

    private boolean matchesAny(String msg, String... keywords) {
        for (String k : keywords) {
            if (msg.contains(k)) return true;
        }
        return false;
    }

    private String extractNumber(String msg) {
        String[] tokens = msg.split("\\s+");
        for (String t : tokens) {
            String cleaned = t.replaceAll("[^0-9.]", "");
            if (!cleaned.isEmpty()) {
                try {
                    return cleaned;
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    private String extractCarType(String msg) {
        String[] knownTypes = {"suv", "sedan", "hatchback", "convertible", "coupe",
                "truck", "van", "minivan", "pickup", "crossover",
                "sports", "electric", "hybrid", "cargo", "family car",
                "luxury", "compact", "compact car", "full size"};
        for (String t : knownTypes) {
            if (msg.contains(t)) return t;
        }
        return null;
    }

    private String extractSpecificCar(String msg) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT carmodel FROM cars")) {
            while (rs.next()) {
                String model = rs.getString("carmodel").toLowerCase(Locale.ROOT);
                if (msg.contains(model)) {
                    return model;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getCustomerName() {
        int userId = Session.getUserId();
        if (userId == -1) return null;
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT username FROM users WHERE id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String buildHelpMessage() {
        return "Here's everything I can help with:\n\n"
                + " CARS\n"
                + "  - \"What cars are available?\"\n"
                + "  - \"What types of cars do you have?\"\n"
                + "  - \"How many cars in total?\"\n"
                + "  - \"Tell me about [car name]\"\n\n"
                + " PRICING\n"
                + "  - \"Price of sedan\"\n"
                + "  - \"How much is a [car name]?\"\n"
                + "  - \"Cheapest car\"\n"
                + "  - \"Most expensive car\"\n"
                + "  - \"Cars under 3000 PKR\"\n\n"
                + " BOOKINGS\n"
                + "  - \"My active bookings\"\n"
                + "  - \"Booking history\"\n"
                + "  - \"How to book a car?\"\n"
                + "  - \"How to return a car?\"\n\n"
                + " SMART QUERIES\n"
                + "  - \"Estimate cost for 5 days\"\n"
                + "  - \"Is a car available on 2025-03-01?\"\n"
                + "  - \"Recommend me a car\"\n"
                + "  - \"Compare SUV vs Sedan\"\n"
                + "  - \"Most popular cars\"\n";
    }

    private String getFleetStats() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) AS total, "
                             + "SUM(CASE WHEN Availability='Yes' THEN 1 ELSE 0 END) AS available "
                             + "FROM cars")) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int available = rs.getInt("available");
                int booked = total - available;
                return "Fleet overview:\n"
                        + "- Total cars: " + total + "\n"
                        + "- Available now: " + available + "\n"
                        + "- Currently booked: " + booked;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Sorry, I couldn't fetch fleet info right now.";
    }

    private String listAvailableCars() {
        StringBuilder sb = new StringBuilder("Available cars:\n\n");
        String query = "SELECT carID, carmodel, cartype, colour, price_per_day "
                + "FROM cars WHERE Availability = 'Yes' ORDER BY price_per_day ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int count = 0;
            while (rs.next()) {
                count++;
                sb.append(String.format("#%d  %s (%s) — %s — %.0f PKR/day%n",
                        rs.getInt("carID"), rs.getString("carmodel"),
                        rs.getString("cartype"), rs.getString("colour"),
                        rs.getDouble("price_per_day")));
            }
            if (count == 0) return "No cars are currently available. Check back soon!";
            sb.append("\n").append(count).append(" car(s) available.");
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't fetch available cars right now.";
        }
        return sb.toString();
    }

    private String getCarTypes() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT cartype, COUNT(*) AS cnt, AVG(price_per_day) AS avgPrice "
                             + "FROM cars GROUP BY cartype ORDER BY cnt DESC")) {

            StringBuilder sb = new StringBuilder("Car types in our fleet:\n\n");
            boolean any = false;
            while (rs.next()) {
                any = true;
                sb.append(String.format("- %s: %d car(s), avg %.0f PKR/day%n",
                        rs.getString("cartype"), rs.getInt("cnt"), rs.getDouble("avgPrice")));
            }
            return any ? sb.toString() : "No car types found in the database.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't fetch car types.";
        }
    }

    private String getCarDetails(String carName) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT carID, carmodel, cartype, colour, price_per_day, Availability "
                             + "FROM cars WHERE LOWER(carmodel) LIKE ?")) {
            ps.setString(1, "%" + carName + "%");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String status = rs.getString("Availability").equalsIgnoreCase("Yes") ? "Available" : "Booked";
                return String.format("Car Details:\n"
                                + "- ID: %d\n"
                                + "- Model: %s\n"
                                + "- Type: %s\n"
                                + "- Colour: %s\n"
                                + "- Price: %.0f PKR/day\n"
                                + "- Status: %s\n\n"
                                + "%s",
                        rs.getInt("carID"), rs.getString("carmodel"),
                        rs.getString("cartype"), rs.getString("colour"),
                        rs.getDouble("price_per_day"), status,
                        status.equals("Available") ? "This car is ready to book!" : "This car is currently rented out.");
            }
            return "I couldn't find a car matching \"" + carName + "\". Try asking \"available cars\" to see the full list.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't look up that car.";
        }
    }

    private String priceForType(String type) {
        String query = "SELECT carID, carmodel, price_per_day FROM cars "
                + "WHERE LOWER(cartype) = ? AND Availability = 'Yes' ORDER BY price_per_day ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder("Prices for " + type + " cars:\n\n");
            boolean any = false;
            while (rs.next()) {
                any = true;
                sb.append(String.format("- #%d %s: %.0f PKR/day%n",
                        rs.getInt("carID"), rs.getString("carmodel"), rs.getDouble("price_per_day")));
            }
            if (!any) {
                // Try to suggest similar types
                return "No available " + type + " cars right now. "
                        + "Try asking \"what types\" to see what we have.";
            }
            return sb.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't look up prices.";
        }
    }

    private String getPriceForCar(String carName) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT carID, carmodel, price_per_day, Availability "
                             + "FROM cars WHERE LOWER(carmodel) LIKE ?")) {
            ps.setString(1, "%" + carName + "%");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return String.format("%s costs %.0f PKR/day (currently %s)",
                        rs.getString("carmodel"), rs.getDouble("price_per_day"),
                        rs.getString("Availability").equalsIgnoreCase("Yes") ? "available" : "booked");
            }
            return "I couldn't find pricing for \"" + carName + "\". Try \"cheapest car\" or \"available cars\".";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't look up that price.";
        }
    }

    private String getCheapestCars() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT carID, carmodel, cartype, price_per_day "
                             + "FROM cars WHERE Availability = 'Yes' "
                             + "ORDER BY price_per_day ASC LIMIT 5")) {

            StringBuilder sb = new StringBuilder("Most affordable cars:\n\n");
            boolean any = false;
            while (rs.next()) {
                any = true;
                sb.append(String.format("- #%d %s (%s): %.0f PKR/day%n",
                        rs.getInt("carID"), rs.getString("carmodel"),
                        rs.getString("cartype"), rs.getDouble("price_per_day")));
            }
            return any ? sb.toString() : "No available cars found.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't fetch prices.";
        }
    }

    private String getMostExpensiveCars() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT carID, carmodel, cartype, price_per_day "
                             + "FROM cars WHERE Availability = 'Yes' "
                             + "ORDER BY price_per_day DESC LIMIT 5")) {

            StringBuilder sb = new StringBuilder("Premium / highest-priced cars:\n\n");
            boolean any = false;
            while (rs.next()) {
                any = true;
                sb.append(String.format("- #%d %s (%s): %.0f PKR/day%n",
                        rs.getInt("carID"), rs.getString("carmodel"),
                        rs.getString("cartype"), rs.getDouble("price_per_day")));
            }
            return any ? sb.toString() : "No available cars found.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't fetch prices.";
        }
    }

    private String getCarsUnderBudget(double maxPrice) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT carID, carmodel, cartype, price_per_day "
                             + "FROM cars WHERE Availability = 'Yes' AND price_per_day <= ? "
                             + "ORDER BY price_per_day ASC")) {
            ps.setDouble(1, maxPrice);
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder("Cars under " + (int) maxPrice + " PKR/day:\n\n");
            boolean any = false;
            while (rs.next()) {
                any = true;
                sb.append(String.format("- #%d %s (%s): %.0f PKR/day%n",
                        rs.getInt("carID"), rs.getString("carmodel"),
                        rs.getString("cartype"), rs.getDouble("price_per_day")));
            }
            return any ? sb.toString() : "No cars found under " + (int) maxPrice + " PKR/day. Try a higher budget.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't search by budget.";
        }
    }

    private String estimateBookingCost(String msg) {
        String daysStr = extractNumber(msg);
        if (daysStr == null) {
            return "How many days would you like to rent for? You can say something like:\n"
                    + "- \"Estimate cost for 5 days\"\n"
                    + "- \"How much for 3 days?\"";
        }

        int days = (int) Double.parseDouble(daysStr);
        if (days <= 0) return "Please specify at least 1 day.";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT carID, carmodel, cartype, price_per_day "
                             + "FROM cars WHERE Availability = 'Yes' ORDER BY price_per_day ASC")) {

            StringBuilder sb = new StringBuilder("Estimated costs for " + days + " day(s):\n\n");
            boolean any = false;
            while (rs.next()) {
                any = true;
                double total = days * rs.getDouble("price_per_day");
                sb.append(String.format("- %s (%s): %.0f x %d = %.0f PKR%n",
                        rs.getString("carmodel"), rs.getString("cartype"),
                        rs.getDouble("price_per_day"), days, total));
            }
            return any ? sb.toString() : "No available cars to estimate for.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't calculate the estimate.";
        }
    }

    private String checkAvailabilityOnDate(String msg) {
        // Try to extract a date from the message
        String dateStr = null;
        String[] datePatterns = {"\\d{4}-\\d{2}-\\d{2}", "\\d{2}/\\d{2}/\\d{4}", "\\d{2}-\\d{2}-\\d{4}"};
        for (String pattern : datePatterns) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(msg);
            if (m.find()) {
                dateStr = m.group();
                break;
            }
        }

        // Check for "today" or "tomorrow"
        if (dateStr == null) {
            if (msg.contains("today")) {
                dateStr = LocalDate.now().toString();
            } else if (msg.contains("tomorrow")) {
                dateStr = LocalDate.now().plusDays(1).toString();
            }
        }

        if (dateStr == null) {
            return "Please specify a date. For example:\n"
                    + "- \"Is a car available on 2025-03-15?\"\n"
                    + "- \"Available today?\"\n"
                    + "- \"Is anything free tomorrow?\"";
        }

        LocalDate checkDate;
        try {
            checkDate = LocalDate.parse(dateStr.replace("/", "-"));
        } catch (Exception e) {
            return "I couldn't understand that date. Please use YYYY-MM-DD format.";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT c.carID, c.carmodel, c.cartype, c.price_per_day "
                             + "FROM cars c "
                             + "WHERE c.Availability = 'Yes' "
                             + "AND NOT EXISTS ("
                             + "  SELECT 1 FROM bookcar b "
                             + "  WHERE b.carID = c.carID "
                             + "  AND b.entrydate <= ? AND b.returndate >= ? "
                             + "  AND (b.returned IS NULL OR b.returned = 0)"
                             + ") "
                             + "ORDER BY c.price_per_day ASC")) {
            ps.setDate(1, java.sql.Date.valueOf(checkDate));
            ps.setDate(2, java.sql.Date.valueOf(checkDate));
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder("Cars available on " + checkDate + ":\n\n");
            boolean any = false;
            while (rs.next()) {
                any = true;
                sb.append(String.format("- #%d %s (%s): %.0f PKR/day%n",
                        rs.getInt("carID"), rs.getString("carmodel"),
                        rs.getString("cartype"), rs.getDouble("price_per_day")));
            }
            return any ? sb.toString() : "Sorry, no cars seem to be available on " + checkDate + ".";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't check availability for that date.";
        }
    }

    private String myActiveBookings() {
        int customerId = Session.getUserId();
        if (customerId == -1) return "Please log in first to see your bookings.";

        String query = """
                SELECT b.carID, c.carmodel, c.cartype, b.entrydate, b.returndate, b.total_cost
                FROM bookcar b
                JOIN cars c ON b.carID = c.carID
                WHERE b.customerID = ? AND (b.returned IS NULL OR b.returned = 0)
                ORDER BY b.entrydate DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder("Your active bookings:\n\n");
            boolean any = false;
            while (rs.next()) {
                any = true;
                long days = ChronoUnit.DAYS.between(
                        rs.getDate("entrydate").toLocalDate(),
                        rs.getDate("returndate").toLocalDate());
                sb.append(String.format("- %s (%s) — %s to %s (%d days) — %.0f PKR%n",
                        rs.getString("carmodel"), rs.getString("cartype"),
                        rs.getDate("entrydate"), rs.getDate("returndate"),
                        days, rs.getDouble("total_cost")));
            }
            if (!any) return "You have no active bookings. Go book a car from the dashboard!";
            return sb.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't fetch your bookings.";
        }
    }

    private String myBookingHistory() {
        int customerId = Session.getUserId();
        if (customerId == -1) return "Please log in first to see your booking history.";

        String query = """
                SELECT b.carID, c.carmodel, b.entrydate, b.returndate, b.total_cost,
                       r.returndate AS actual_return, r.fuellevel, r.carcondition
                FROM bookcar b
                JOIN cars c ON b.carID = c.carID
                LEFT JOIN returncar r ON b.carID = r.carID AND b.customerID = r.customerID
                WHERE b.customerID = ?
                ORDER BY b.entrydate DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder("Your booking history:\n\n");
            boolean any = false;
            while (rs.next()) {
                any = true;
                String status = (rs.getObject("actual_return") == null) ? "Active" : "Returned";
                sb.append(String.format("- %s: %s to %s — %.0f PKR [%s]",
                        rs.getString("carmodel"),
                        rs.getDate("entrydate"), rs.getDate("returndate"),
                        rs.getDouble("total_cost"), status));
                if (status.equals("Returned")) {
                    sb.append(String.format(" (Fuel: %d%%, Condition: %s)",
                            rs.getInt("fuellevel"), rs.getString("carcondition")));
                }
                sb.append("\n");
            }
            return any ? sb.toString() : "You have no booking history yet.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't fetch your history.";
        }
    }

    private String getRecommendation(String msg) {
        String budgetStr = extractNumber(msg);
        double budget = (budgetStr != null) ? Double.parseDouble(budgetStr) : -1;

        StringBuilder sb = new StringBuilder();

        if (budget > 0) {
            sb.append("Cars within your ").append((int) budget).append(" PKR/day budget:\n\n");
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT carID, carmodel, cartype, price_per_day "
                                 + "FROM cars WHERE Availability = 'Yes' AND price_per_day <= ? "
                                 + "ORDER BY price_per_day DESC LIMIT 3")) {
                ps.setDouble(1, budget);
                ResultSet rs = ps.executeQuery();
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    sb.append(String.format("- %s (%s): %.0f PKR/day%n",
                            rs.getString("carmodel"), rs.getString("cartype"),
                            rs.getDouble("price_per_day")));
                }
                if (!any) return "No cars under " + (int) budget + " PKR/day. Try a higher budget!";
            } catch (SQLException e) {
                e.printStackTrace();
                return "Sorry, I couldn't find recommendations.";
            }
        } else {
            // Overall top recommendations based on popularity and value
            sb.append("My top recommendations:\n\n");
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT c.carID, c.carmodel, c.cartype, c.price_per_day, "
                                 + "COUNT(b.bookcarID) AS bookings "
                                 + "FROM cars c "
                                 + "LEFT JOIN bookcar b ON c.carID = b.carID "
                                 + "WHERE c.Availability = 'Yes' "
                                 + "GROUP BY c.carID, c.carmodel, c.cartype, c.price_per_day "
                                 + "ORDER BY bookings DESC, c.price_per_day ASC LIMIT 3")) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    int bookings = rs.getInt("bookings");
                    sb.append(String.format("- %s (%s): %.0f PKR/day — booked %d time(s)%n",
                            rs.getString("carmodel"), rs.getString("cartype"),
                            rs.getDouble("price_per_day"), bookings));
                }
                if (!any) return "No cars available for recommendation right now.";
            } catch (SQLException e) {
                e.printStackTrace();
                return "Sorry, I couldn't generate recommendations.";
            }
        }

        sb.append("\nTip: Say \"recommend under 5000\" to filter by budget!");
        return sb.toString();
    }

    private String compareCars(String msg) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT cartype, AVG(price_per_day) AS avgPrice, COUNT(*) AS cnt "
                             + "FROM cars GROUP BY cartype HAVING COUNT(*) > 0 ORDER BY cnt DESC")) {

            StringBuilder sb = new StringBuilder("Compare by car type:\n\n");
            sb.append(String.format("%-15s %-8s %-12s%n", "Type", "Count", "Avg Price"));
            sb.append("-".repeat(40)).append("\n");

            boolean any = false;
            while (rs.next()) {
                any = true;
                sb.append(String.format("%-15s %-8d %-12.0f%n",
                        rs.getString("cartype"), rs.getInt("cnt"), rs.getDouble("avgPrice")));
            }

            if (!any) return "Not enough data to compare. Add more cars first!";

            sb.append("\nTip: You can also ask about specific types like \"price of sedan\" vs \"price of suv\"");
            return sb.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't compare cars right now.";
        }
    }

    private String getMostBookedCars() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT c.carID, c.carmodel, c.cartype, c.price_per_day, "
                             + "COUNT(b.bookcarID) AS timesBooked "
                             + "FROM cars c "
                             + "JOIN bookcar b ON c.carID = b.carID "
                             + "GROUP BY c.carID, c.carmodel, c.cartype, c.price_per_day "
                             + "ORDER BY timesBooked DESC LIMIT 5")) {

            StringBuilder sb = new StringBuilder("Most popular cars (by bookings):\n\n");
            boolean any = false;
            int rank = 1;
            while (rs.next()) {
                any = true;
                sb.append(String.format("%d. %s (%s) — %.0f PKR/day — booked %d time(s)%n",
                        rank++, rs.getString("carmodel"), rs.getString("cartype"),
                        rs.getDouble("price_per_day"), rs.getInt("timesBooked")));
            }
            return any ? sb.toString() : "No booking data yet. Be the first to book!";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I couldn't fetch popular cars.";
        }
    }

    private String handleYesFollowUp() {
        switch (lastIntent) {
            case "greeting" -> {
                lastIntent = "none";
                return "Great! What would you like to know? I can help with cars, prices, bookings, and more.";
            }
            case "available_cars" -> {
                lastIntent = "none";
                return "Head to Dashboard → Book a Car to reserve one! You'll need the Car ID from the list above.";
            }
            case "my_bookings" -> {
                lastIntent = "none";
                return "You can return a car from Dashboard → Return Vehicle. Just have the Car ID ready!";
            }
            default -> {
                lastIntent = "none";
                return "What would you like to know? Just ask me anything about cars, prices, or bookings!";
            }
        }
    }

    private String handleNoFollowUp() {
        switch (lastIntent) {
            case "my_bookings" -> {
                lastIntent = "none";
                return "No problem! Let me know if you need help with anything else.";
            }
            case "available_cars" -> {
                lastIntent = "none";
                return "Alright! You can ask me about prices or car types anytime.";
            }
            default -> {
                lastIntent = "none";
                return "No worries! I'm here whenever you need help. Just ask away!";
            }
        }
    }

    private String pickRandom(String... options) {
        return options[new Random().nextInt(options.length)];
    }
}
