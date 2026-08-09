module com.example.carrental {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;
    requires java.sql;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires jakarta.mail;
    requires org.eclipse.angus.mail;
    requires jakarta.activation;

    opens com.example.carrental to javafx.fxml;
    exports com.example.carrental;
}
