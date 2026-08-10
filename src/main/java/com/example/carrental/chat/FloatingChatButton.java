package com.example.carrental.chat;

import com.example.carrental.ai.chatbot.IntentClassifier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FloatingChatButton {

    private static final String[] SUGGESTIONS = {
            "What cars are available?",
            "Price of sedan",
            "Cheapest car",
            "My bookings",
            "Estimate cost for 3 days",
            "Recommend me a car",
            "I want to rent a sedan",
            "What types of cars?",
            "How many cars in total?",
            "Is a car available today?",
            "Compare car types",
            "Most popular cars",
            "Help"
    };

    public static void install(Pane parent) {
        Button chatBtn = new Button("\uD83D\uDE97");
        chatBtn.setFont(Font.font("System", 22));
        chatBtn.setPrefSize(52, 52);
        chatBtn.setMinSize(52, 52);
        chatBtn.setMaxSize(52, 52);
        chatBtn.setStyle(
                "-fx-background-color: #00b4d8;"
                        + "-fx-background-radius: 26;"
                        + "-fx-text-fill: white;"
                        + "-fx-cursor: hand;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 4);"
        );

        chatBtn.setOnMouseEntered(e -> chatBtn.setStyle(
                "-fx-background-color: #00d4ff;"
                        + "-fx-background-radius: 26;"
                        + "-fx-text-fill: white;"
                        + "-fx-cursor: hand;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 16, 0, 0, 4);"
                        + "-fx-scale-x: 1.1; -fx-scale-y: 1.1;"
        ));
        chatBtn.setOnMouseExited(e -> chatBtn.setStyle(
                "-fx-background-color: #00b4d8;"
                        + "-fx-background-radius: 26;"
                        + "-fx-text-fill: white;"
                        + "-fx-cursor: hand;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 4);"
        ));

        AnchorPane.setRightAnchor(chatBtn, 20.0);
        AnchorPane.setBottomAnchor(chatBtn, 80.0);

        chatBtn.setOnAction(e -> openChatWindow(chatBtn));

        parent.getChildren().add(chatBtn);
    }

    private static void openChatWindow(Button source) {
        Stage popup = new Stage();
        popup.initStyle(StageStyle.UTILITY);
        popup.setTitle("Car Rental Assistant");
        popup.setResizable(false);
        popup.setWidth(420);
        popup.setHeight(560);

        IntentClassifier classifier = new IntentClassifier();
        ObservableList<String> messages = FXCollections.observableArrayList();
        messages.add("Bot: Hi! Ask me about cars, prices, or bookings!");

        VBox root = new VBox();
        root.setSpacing(0);
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("  Car Assistant");
        title.setStyle("-fx-text-fill: #00b4d8; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 12 16;");
        title.setPrefHeight(44);

        FlowPane suggestionsPane = new FlowPane();
        suggestionsPane.setHgap(6);
        suggestionsPane.setVgap(6);
        suggestionsPane.setPadding(new Insets(8, 12, 8, 12));
        suggestionsPane.setStyle("-fx-background-color: rgba(255,255,255,0.04);");

        for (String s : SUGGESTIONS) {
            Button sb = new Button(s);
            sb.setStyle(
                    "-fx-background-color: rgba(0,180,216,0.15);"
                            + "-fx-text-fill: #7ec8e3;"
                            + "-fx-font-size: 11px;"
                            + "-fx-background-radius: 6;"
                            + "-fx-padding: 4 8;"
                            + "-fx-cursor: hand;"
            );
            sb.setOnMouseEntered(e -> sb.setStyle(
                    "-fx-background-color: rgba(0,180,216,0.3);"
                            + "-fx-text-fill: white;"
                            + "-fx-font-size: 11px;"
                            + "-fx-background-radius: 6;"
                            + "-fx-padding: 4 8;"
                            + "-fx-cursor: hand;"
            ));
            sb.setOnMouseExited(e -> sb.setStyle(
                    "-fx-background-color: rgba(0,180,216,0.15);"
                            + "-fx-text-fill: #7ec8e3;"
                            + "-fx-font-size: 11px;"
                            + "-fx-background-radius: 6;"
                            + "-fx-padding: 4 8;"
                            + "-fx-cursor: hand;"
            ));
            suggestionsPane.getChildren().add(sb);
        }

        ListView<String> chatLog = new ListView<>(messages);
        chatLog.setPrefHeight(320);
        chatLog.setStyle(
                "-fx-background-color: #0f0f23;"
                        + "-fx-control-inner-background: #0f0f23;"
                        + "-fx-font-size: 13px;"
        );
        chatLog.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    label.setWrapText(true);
                    label.setMaxWidth(370);
                    if (item.startsWith("You:")) {
                        label.setStyle("-fx-text-fill: #90caf9; -fx-font-size: 13px; -fx-padding: 4 8;");
                    } else {
                        label.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 13px; -fx-padding: 4 8;");
                    }
                    setGraphic(label);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        HBox inputBar = new HBox();
        inputBar.setSpacing(8);
        inputBar.setPadding(new Insets(10, 12, 12, 12));
        inputBar.setAlignment(Pos.CENTER);
        inputBar.setStyle("-fx-background-color: #16213e;");

        TextField inputField = new TextField();
        inputField.setPromptText("Ask anything...");
        inputField.setPrefHeight(38);
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.1);"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 13px;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 0 10;"
        );

        Button sendBtn = new Button("Send");
        sendBtn.setPrefSize(65, 38);
        sendBtn.setStyle(
                "-fx-background-color: #27ae60;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 13px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
                        + "-fx-cursor: hand;"
        );

        Runnable sendAction = () -> {
            String msg = inputField.getText().trim();
            if (msg.isEmpty()) return;
            messages.add("You: " + msg);
            String response = classifier.respond(msg);
            messages.add("Bot: " + response);
            inputField.clear();
            chatLog.scrollTo(messages.size() - 1);
        };

        sendBtn.setOnAction(e -> sendAction.run());
        inputField.setOnAction(e -> sendAction.run());

        inputBar.getChildren().addAll(inputField, sendBtn);

        suggestionsPane.getChildren().forEach(node -> {
            if (node instanceof Button sb) {
                sb.setOnAction(e -> {
                    inputField.setText(sb.getText());
                    sendAction.run();
                });
            }
        });

        root.getChildren().addAll(title, suggestionsPane, chatLog, inputBar);

        Scene scene = new Scene(root, 420, 560);
        popup.setScene(scene);
        popup.show();

        inputField.requestFocus();
    }
}
