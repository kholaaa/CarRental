package car.rental.system;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class Loading2Controller implements Initializable {

    @FXML
    private ImageView logoImage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Load image (NO absolute path)
        Image image = new Image(
                getClass().getResourceAsStream("/car/rental/system/loading2.pic.jpg")
        );
        logoImage.setImage(image);

        // Auto close after 3 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            Stage stage = (Stage) logoImage.getScene().getWindow();
            stage.close();

            // Open login screen
            new Login().start(new Stage());
        });
        delay.play();
    }
}
