import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class JavaFXClient extends Application {

    public static Text text;
    public static int counter = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Pane rootPane = new Pane();

        Stage stage = new Stage();
        stage.setTitle("JavaFX Dynamic Scene Demo");
        stage.setResizable(false);
        stage.setScene(new Scene(rootPane));
        stage.setWidth(1280);
        stage.setHeight(1024);
        stage.setOnCloseRequest((WindowEvent we) -> System.exit(0));
        stage.show();

        text = new Text();
        text.setFont(new Font("Open Sans", 300));
        text.setText("...");
        text.setLayoutX(200);
        text.setLayoutY(600);
        rootPane.getChildren().add(text);

        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(100),
                ae -> doSomething()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

    }

    public static void doSomething() {

        counter = (int) ((System.currentTimeMillis() / 100) % 100000);
        text.setText(Integer.toString(counter));

    }
}
