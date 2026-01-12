package com.example.carrental;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.AnchorPane;

public class loading2Controller {
    @FXML
    private Label welcomeLabel;

    @FXML
    private AnchorPane rootPane;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Loading.........");


        Image bgImage = new Image(getClass().getResourceAsStream("/com/example/carrental/pics/llg.png"));
        BackgroundImage backgroundImage = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
        rootPane.setBackground(new Background(backgroundImage));
    }
}
