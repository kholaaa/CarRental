package car.rental.system;

import java.sql.*;

public class DBConnection {

    static final String URL = "jdbc:mysql://127.0.0.1:3306/CarRental"; // replace with your DB name
    static final String USER = "root";
    static final String PASSWORD = "ot10forever10@";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
