module com.example.carrental {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;

    opens com.example.carrental to javafx.fxml;  // ← This allows reflection access
    exports com.example.carrental;
}