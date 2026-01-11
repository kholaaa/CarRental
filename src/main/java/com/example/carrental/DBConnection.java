package com.example.carrental;

import java.sql.*;

public class DBConnection {

    // Add your database name 'CarRental' after the port
    static final String URL = "jdbc:mysql://127.0.0.1:3306/CarRental?user=root";
    static final String USER = "root";
    static final String PASSWORD = "ot10forever10@";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}