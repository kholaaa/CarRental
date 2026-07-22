package com.example.carrental;

import com.example.carrental.chat.FloatingChatButton;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class TermsConditionsController {

    @FXML private AnchorPane rootPane;
    @FXML private Label termsTitleLabel;
    @FXML private Label termsContentLabel;
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        FloatingChatButton.install(rootPane);

        boolean isAdmin = Session.isAdmin();

        if (isAdmin) {
            termsTitleLabel.setText("Administrator Terms & Conditions");
            termsContentLabel.setText(
                    "By accessing the administrative dashboard, you agree to the following:\n\n"
                            + "1. DATA CONFIDENTIALITY\n"
                            + "   Maintain strict confidentiality of all customer and company data. "
                            + "Do not share, copy, or transfer sensitive information to unauthorized parties.\n\n"
                            + "2. AUTHORIZED USE\n"
                            + "   Use administrative privileges exclusively for authorized business purposes. "
                            + "Any misuse of admin access will result in immediate account suspension.\n\n"
                            + "3. SECURITY REPORTING\n"
                            + "   Immediately report any suspected security breaches, vulnerabilities, "
                            + "or policy violations to the IT security team.\n\n"
                            + "4. REGULATORY COMPLIANCE\n"
                            + "   Comply fully with all applicable data protection laws and regulations, "
                            + "including but not limited to privacy Acts and local ordinances.\n\n"
                            + "5. ACCOUNTABILITY\n"
                            + "   Accept complete responsibility for all actions performed under your account. "
                            + "Admin activities are logged and subject to audit at any time.\n\n"
                            + "6. ACCEPTABLE USE POLICY\n"
                            + "   Do not attempt to access systems, databases, or accounts beyond your "
                            + "authorized scope. Report any privilege escalation issues immediately."
            );
        } else {
            termsTitleLabel.setText("Customer Terms & Conditions");
            termsContentLabel.setText(
                    "By using our car rental services, you agree to the following terms:\n\n"
                            + "1. VEHICLE CONDITION\n"
                            + "   Return the vehicle in the same condition and with the same fuel level as received. "
                            + "Any discrepancies will be charged accordingly.\n\n"
                            + "2. DAMAGE REPORTING\n"
                            + "   Report any accidents, damage, or mechanical issues immediately upon discovery. "
                            + "Failure to report may result in additional liability.\n\n"
                            + "3. TRAFFIC VIOLATIONS\n"
                            + "   Be fully responsible for any traffic violations, parking fines, or "
                            + "toll charges incurred during the rental period.\n\n"
                            + "4. LAWS & RESTRICTIONS\n"
                            + "   Comply with all applicable traffic laws and vehicle usage restrictions. "
                            + "The vehicle must not be used for illegal activities or off-road driving.\n\n"
                            + "5. ADDITIONAL CHARGES\n"
                            + "   Accept responsibility for additional charges related to late returns, "
                            + "interior cleaning, excessive wear, or damage beyond normal use.\n\n"
                            + "6. IDENTIFICATION\n"
                            + "   Provide valid identification and a valid driver's license meeting all "
                            + "legal requirements before taking possession of the vehicle."
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
