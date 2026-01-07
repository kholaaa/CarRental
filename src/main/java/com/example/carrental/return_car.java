package car.rental.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(
                FXMLLoader.load(getClass().getResource("return_car.fxml"))
        ));
        stage.setTitle("Return Car");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}