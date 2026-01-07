import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class add_car extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("add_car.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Add Car");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}