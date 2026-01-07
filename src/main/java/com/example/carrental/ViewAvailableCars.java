package car.rental.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewAvailableCarsApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(
                FXMLLoader.load(getClass().getResource(
                        "/car/rental/system/view_available_cars.fxml"))
        ));
        stage.setTitle("Available Cars");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}