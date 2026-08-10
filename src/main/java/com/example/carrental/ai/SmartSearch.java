package com.example.carrental.ai;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Natural-language search ranking.
 * Supports:
 *  - fuzzy, typo-tolerant keyword matching (Levenshtein-based)
 *  - multi-word queries ("toyota corolla")
 *  - budget filters ("under 5000", "below 3000", "less than 4000", "< 5000")
 *  - intent words ("cheap"/"affordable" prefer low prices, "luxury"/"premium" prefer high prices)
 */
public class SmartSearch {

    public interface Searchable {
        String getSearchableText();
    }

    /** Items that also expose their price allow budget/cheap/luxury ranking. */
    public interface Priced extends Searchable {
        double getPrice();
    }

    private static final Pattern BUDGET_PATTERN =
            Pattern.compile("(under|below|less than|cheaper than|budget of|max)\\s*([0-9,]+)");
    private static final Pattern BUDGET_SYMBOL_PATTERN =
            Pattern.compile("<\\s*([0-9,]+)");
    private static final Pattern ANY_NUMBER_PATTERN = Pattern.compile("\\d[0-9,]*");

    private static final Set<String> FILTER_WORDS = Set.of(
            "show", "me", "a", "an", "the", "and", "or", "for", "with", "cars", "car",
            "cheap", "cheapest", "affordable", "budget", "inexpensive", "low", "lowest",
            "expensive", "luxury", "premium", "high", "highest", "cost", "price", "priced",
            "under", "below", "less", "than", "max", "automatic", "manual", "auto");

    /** Returns items re-ordered by relevance to the query. Empty query = original order. */
    public static <T extends Searchable> List<T> rank(String query, List<T> items) {
        if (query == null || query.trim().isEmpty()) return items;

        String q = query.trim().toLowerCase(Locale.ROOT);
        Double budget = extractBudget(q);
        boolean wantCheap = containsAny(q, "cheap", "cheapest", "affordable", "budget", "inexpensive", "low cost", "low price", "under", "below");
        boolean wantLuxury = containsAny(q, "expensive", "luxury", "premium", "high price", "highest");

        // Remove filter/intent words so they don't pollute the model match
        String matchQuery = stripFilterWords(q);

        List<Map.Entry<T, Double>> scored = new ArrayList<>();
        for (T item : items) {
            String text = item.getSearchableText().toLowerCase(Locale.ROOT);
            int tokenScore = tokenMatchScore(matchQuery, text);

            double score = tokenScore;
            boolean priceFiltered = false;

            if (item instanceof Priced p) {
                if (budget != null && p.getPrice() > budget) {
                    priceFiltered = true; // over budget
                } else {
                    if (wantCheap) score += cheapBoost(p.getPrice());
                    if (wantLuxury) score += luxuryBoost(p.getPrice());
                }
            }

            // Query was only about price (budget/cheap/luxury): still show items, ordered by price
            if (tokenScore == 0 && (budget != null || wantCheap || wantLuxury)) {
                score = Math.max(score, 1.0);
            }

            if (!priceFiltered && score > 0) scored.add(Map.entry(item, score));
        }

        scored.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        List<T> result = new ArrayList<>();
        for (Map.Entry<T, Double> e : scored) result.add(e.getKey());
        return result;
    }

    private static int tokenMatchScore(String query, String text) {
        if (query == null || query.trim().isEmpty()) return 0;
        String q = query.trim();
        if (text.contains(q)) return 100;

        String[] qTokens = q.split("\\s+");
        int total = 0;
        for (String qt : qTokens) {
            if (qt.length() < 3) continue;
            int best = 0;
            for (String t : text.split("\\s+")) {
                int dist = levenshtein(qt, t);
                int maxLen = Math.max(qt.length(), t.length());
                if (maxLen == 0) continue;
                double similarity = 1.0 - (double) dist / maxLen;
                if (similarity > 0.6) {
                    best = Math.max(best, (int) (similarity * 80));
                }
            }
            total += best;
        }
        return total;
    }

    private static Double extractBudget(String q) {
        Matcher m = BUDGET_PATTERN.matcher(q);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(2).replace(",", ""));
            } catch (NumberFormatException ignored) {}
        }
        Matcher m2 = BUDGET_SYMBOL_PATTERN.matcher(q);
        if (m2.find()) {
            try {
                return Double.parseDouble(m2.group(1).replace(",", ""));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private static String stripFilterWords(String q) {
        String[] tokens = q.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String t : tokens) {
            if (t.matches("\\d[0-9,]*") || FILTER_WORDS.contains(t)) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(t);
        }
        return sb.toString();
    }

    private static double cheapBoost(double price) {
        return Math.max(0.0, 60.0 - price / 100.0);
    }

    private static double luxuryBoost(double price) {
        return Math.max(0.0, price / 100.0 - 20.0);
    }

    private static boolean containsAny(String q, String... words) {
        for (String w : words) {
            if (q.contains(w)) return true;
        }
        return false;
    }

    private static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }
}
