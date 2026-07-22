package com.example.carrental;

import com.example.carrental.ai.chatbot.IntentClassifier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class ChatbotController {

    @FXML private TextField inputField;
    @FXML private ListView<String> chatLog;
    @FXML private AnchorPane rootPane;
    @FXML private FlowPane suggestionsPane;

    private final IntentClassifier classifier = new IntentClassifier();
    private final ObservableList<String> messages = FXCollections.observableArrayList();

    private static final String[] SUGGESTIONS = {
            "What cars are available?",
            "Price of sedan",
            "Cheapest car",
            "My bookings",
            "Estimate cost for 3 days",
            "Recommend me a car",
            "What types of cars?",
            "How many cars in total?",
            "Is a car available today?",
            "Compare car types",
            "Most popular cars",
            "How to book a car?",
            "How to return a car?",
            "Help"
    };

    @FXML
    public void initialize() {
        loadBackgroundImage();
        chatLog.setItems(messages);
        messages.add("Bot: Hi! I'm your car rental assistant. Ask me anything or tap a suggestion below!");
        loadSuggestions();
    }

    private void loadSuggestions() {
        suggestionsPane.getChildren().clear();
        for (String suggestion : SUGGESTIONS) {
            Button btn = new Button(suggestion);
            btn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.12);"
                            + "-fx-text-fill: #aadcff;"
                            + "-fx-font-size: 12px;"
                            + "-fx-background-radius: 8;"
                            + "-fx-border-color: rgba(100,180,255,0.3);"
                            + "-fx-border-radius: 8;"
                            + "-fx-padding: 6 12 6 12;"
                            + "-fx-cursor: hand;"
            );
            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-background-color: rgba(100,180,255,0.25);"
                            + "-fx-text-fill: #ffffff;"
                            + "-fx-font-size: 12px;"
                            + "-fx-background-radius: 8;"
                            + "-fx-border-color: rgba(100,180,255,0.6);"
                            + "-fx-border-radius: 8;"
                            + "-fx-padding: 6 12 6 12;"
                            + "-fx-cursor: hand;"
            ));
            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.12);"
                            + "-fx-text-fill: #aadcff;"
                            + "-fx-font-size: 12px;"
                            + "-fx-background-radius: 8;"
                            + "-fx-border-color: rgba(100,180,255,0.3);"
                            + "-fx-border-radius: 8;"
                            + "-fx-padding: 6 12 6 12;"
                            + "-fx-cursor: hand;"
            ));
            btn.setOnAction(e -> {
                inputField.setText(suggestion);
                handleSend(null);
            });
            suggestionsPane.getChildren().add(btn);
        }
    }

    private void loadBackgroundImage() {
        String imagePath = "/com/example/carrental/pics/llg.png";

        try (InputStream stream = getClass().getResourceAsStream(imagePath)) {
            if (stream == null) {
                rootPane.setStyle("-fx-background-color: #0f171e;");
                return;
            }

            Image bgImage = new Image(stream);
            BackgroundImage backgroundImage = new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
            );
            rootPane.setBackground(new Background(backgroundImage));

        } catch (Exception e) {
            e.printStackTrace();
            rootPane.setStyle("-fx-background-color: #0f171e;");
        }
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String msg = inputField.getText().trim();
        if (msg.isEmpty()) return;

        messages.add("You: " + msg);
        String response = classifier.respond(msg);
        messages.add("Bot: " + response);

        inputField.clear();
        chatLog.scrollTo(messages.size() - 1);
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/com/example/carrental/Dashboard.fxml"));
        Scene scene = new Scene(dashboardRoot);
        stage.setScene(scene);
        stage.centerOnScreen();
    }
}
