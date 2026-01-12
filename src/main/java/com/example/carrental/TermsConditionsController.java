package com.example.carrental;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class TermsConditionsController {

    // FXML Components
    @FXML private Label termsTitleLabel;
    @FXML private Label termsContentLabel;
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        boolean isAdmin = Session.isAdmin();  // Assuming Session class exists

        if (isAdmin) {
            termsTitleLabel.setText("Administrator Terms & Conditions");
            termsContentLabel.setText(
                    "By accessing the administrative dashboard, you agree to the following:\n\n" +
                            "• Maintain strict confidentiality of all customer and company data\n" +
                            "• Use administrative privileges exclusively for authorized business purposes\n" +
                            "• Immediately report any suspected security issues or policy violations\n" +
                            "• Comply fully with applicable data protection laws and regulations\n" +
                            "• Accept complete responsibility for all actions performed under your account\n" +
                            "• Understand that all administrative activities are logged and auditable"
            );
        } else {
            termsTitleLabel.setText("Customer Terms & Conditions");
            termsContentLabel.setText(
                    "By using our car rental services, you agree to the following terms:\n\n" +
                            "• Return the vehicle in the same condition and with the same fuel level as received\n" +
                            "• Report any accidents, damage, or mechanical issues immediately upon discovery\n" +
                            "• Be fully responsible for any traffic violations or parking fines during the rental period\n" +
                            "• Comply with all applicable traffic laws and vehicle usage restrictions\n" +
                            "• Accept responsibility for additional charges related to late returns, cleaning, or damage\n" +
                            "• Provide valid identification and a driver's license meeting all legal requirements"
            );
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        String resourcePath = "/com/example/carrental/Dashboard.fxml";
        Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}