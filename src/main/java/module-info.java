module com.example.carrental {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.carrental to javafx.fxml;  // ← This allows reflection access
    exports com.example.carrental;
}