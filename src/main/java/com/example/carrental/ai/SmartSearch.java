package com.example.carrental.ai;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Fuzzy, typo-tolerant search ranking (Levenshtein-distance based).
 * Works on any list of items that expose searchable text, so it can be
 * reused for cars, customers, etc.
 */
public class SmartSearch {

    public interface Searchable {
        String getSearchableText();
    }

    /** Returns items re-ordered by relevance to the query. Empty query = original order. */
    public static <T extends Searchable> List<T> rank(String query, List<T> items) {
        if (query == null || query.trim().isEmpty()) return items;

        String q = query.trim().toLowerCase();

        List<Map.Entry<T, Integer>> scored = new ArrayList<>();
        for (T item : items) {
            String text = item.getSearchableText().toLowerCase();
            int score = score(q, text);
            if (score > 0) scored.add(Map.entry(item, score));
        }

        scored.sort((a, b) -> b.getValue() - a.getValue());
        return scored.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private static int score(String query, String text) {
        if (text.contains(query)) return 100; // exact substring match wins

        String[] tokens = text.split("\\s+");
        int best = 0;
        for (String token : tokens) {
            int dist = levenshtein(query, token);
            int maxLen = Math.max(query.length(), token.length());
            if (maxLen == 0) continue;
            double similarity = 1.0 - (double) dist / maxLen;
            if (similarity > 0.6) { // tolerate small typos, ignore unrelated words
                best = Math.max(best, (int) (similarity * 80));
            }
        }
        return best;
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