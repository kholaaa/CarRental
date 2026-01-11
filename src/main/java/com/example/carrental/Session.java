package com.example.carrental;

public class Session {
    private static int userId = -1;
    private static String role = "customer"; // Default

    public static void setUser(int id, String userRole) {
        userId = id;
        role = (userRole != null && !userRole.isEmpty()) ? userRole : "customer";
    }

    public static int getUserId() {
        return userId;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isAdmin() {
        return "admin".equals(role);
    }

    public static void clear() {
        userId = -1;
        role = "customer";
    }
}