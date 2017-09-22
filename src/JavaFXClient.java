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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JavaFXClient extends Application {

    // - - - - - - - - DEPLOYED SETTINGS - - - - - - - - //
    //public static final String serverAddress = "services.farnborough.ac.uk";
    //public static final boolean fullscreen = true;
    //  - - - - - - - -  - - - - - - - -  - - - - - - - - //

    // - - - - - - - - DEVELOPMENT SETTINGS  - - - - - //
    public static final String serverAddress = "localhost";
    public static final boolean fullscreen = false;
    //  - - - - - - - -  - - - - - - - -  - - - - - - - - //

    public static Text text;
    public static int counter = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Pane rootPane = new Pane();

        Stage stage = new Stage();
        stage.setTitle("Room test...");
        stage.setResizable(false);
        stage.setFullScreen(fullscreen);
        stage.setScene(new Scene(rootPane));
        stage.setWidth(1280);
        stage.setHeight(1024);
        stage.setOnCloseRequest((WindowEvent we) -> System.exit(0));
        stage.show();

        text = new Text();
        text.setFont(new Font("Open Sans", 400));
        text.setText("");
        text.setLayoutX(0);
        text.setLayoutY(600);
        rootPane.getChildren().add(text);

        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(250),
                ae -> doSomething()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

    }

    public static void doSomething() {

        counter = (int) ((System.currentTimeMillis() / 250) % 100000);

        URL url;
        HttpURLConnection con;

        try {
            url = new URL( "http://" + serverAddress + ":8081?index=" + counter);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            System.out.println("HTTP GET URL: " + url + ", Response Code: " + responseCode);
            InputStream inputStream = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line="";
            while(br.ready()){
                line = br.readLine();
            }
            text.setText(line);

        }
        catch (Exception ex) {
            System.out.println("HTTP GET ERROR: " + ex.getMessage());
        }

    }
}
