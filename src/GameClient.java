import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GameClient extends Application {

    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 1024;

    static HashSet<KeyCode> keysPressed = new HashSet<>();
    static final ArrayList<Entity> currentEntities = new ArrayList<>();
    static int viewportPosition = 0;

    // - - - - - - - - DEPLOYED SETTINGS - - - - - - - - //
    //public static final String serverAddress = "services.farnborough.ac.uk";
    //public static final boolean fullscreen = true;
    //  - - - - - - - -  - - - - - - - -  - - - - - - - - //

    // - - - - - - - - DEVELOPMENT SETTINGS  - - - - - //
    public static final String serverAddress = "localhost";
    public static final boolean fullscreen = false;
    //  - - - - - - - -  - - - - - - - -  - - - - - - - - //

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Pane rootPane = new Pane();
        Scene scene = new Scene(rootPane);

        Stage stage = new Stage();
        stage.setTitle("Room test...");
        stage.setResizable(false);
        stage.setFullScreen(fullscreen);
        stage.setScene(scene);
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.setOnCloseRequest((WindowEvent we) -> System.exit(0));
        stage.show();

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> keysPressed.add(event.getCode()));
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> keysPressed.remove(event.getCode()));

        Canvas canvas = new Canvas();

        canvas.setWidth(WINDOW_WIDTH);
        canvas.setHeight(WINDOW_HEIGHT);

        rootPane.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.WHITE);

        Image image = new Image("/sprite.png");

        new AnimationTimer() {
            @Override
            public void handle(long now) {

                for(KeyCode k : keysPressed) {
                    if (k == KeyCode.ESCAPE) System.exit(0);
                }

                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

                long time = System.currentTimeMillis() >> 8;
                double offset = (System.currentTimeMillis() % 256) / 256.0;

                synchronized (currentEntities) {
                    for (Entity e : currentEntities) {
                        //System.out.println("X: " + e.x + ", Y:" + e.y);

                        int x0 = -1;
                        int y0 = -1;
                        int x1 = -1;
                        int y1 = -1;

                        //System.out.print("[" + time + "] " + e.getId() + ": ");
                        for (long t: e.xMap.keySet()) {
                            //System.out.print(t + ", ");
                            if (t == time) {
                                x0 = e.xMap.get(t);
                                y0 = e.yMap.get(t);
                            }
                            else if (t == time + 1) {
                                x1 = e.xMap.get(t);
                                y1 = e.yMap.get(t);
                            }
                        }

                        //System.out.println();

                        if (x0 != -1 && y0 != -1 && x1 != -1 && y1 != -1) {
                            int x = (int) (64.0 * (x0 + offset * (x1 - x0)));
                            int y = (int) (64.0 * (y0 + offset * (y1 - y0)));
                            gc.drawImage(image, x - viewportPosition * WINDOW_WIDTH, y );
                        }
                        //else {
                        //    System.out.println(time + ": " + x0 + ", " + y0 + ", " + x1 + ", " + y1);
                        //}

                    }
                }

            }
        }.start();

        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(256),
                ae -> getUpdate()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

    }

    public static void getUpdate() {

        long clientTime = System.currentTimeMillis() >> 8;
        HashMap<Integer, Entity> entities = new HashMap<>();

        URL url;
        HttpURLConnection con;

        try {
            url = new URL( "http://" + serverAddress + ":8081?index=" + clientTime);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            //System.out.println("HTTP GET URL: " + url + ", Response Code: " + responseCode);
            InputStream inputStream = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String inputjson="";
            while(br.ready()){
                inputjson = br.readLine();
            }

            //System.out.println("RECEIVED: " + inputjson);

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(inputjson);
            JSONObject jsonObject = (JSONObject)obj;

            //HashMap<Long, ArrayList<Entity>> frames = new HashMap<>();

            if (jsonObject.containsKey("frames")) {

                //for (Object key: jsonObject.keySet()) {
//                if (key.toString().toLowerCase().equals("frames")) {

                JSONArray frameArray = (JSONArray) jsonObject.get("frames");
                for (Object frameObject : frameArray) {
                    JSONObject frame = (JSONObject) frameObject;

                    long time = -1;

                    if (frame.containsKey("time") && frame.containsKey("position") && frame.containsKey("entities")) {

                        //for (Object frameKey: frame.keySet()) {
                        //    String frameKeyString = frameKey.toString().toLowerCase();
                        //if (frameKeyString.equals("time")) {

                        time = Long.parseLong(frame.get("time").toString());

                        viewportPosition = Integer.parseInt(frame.get("position").toString()) - 1;

                        //}
                        //else if (frameKeyString.equals("entities")) {

                        JSONArray entityArray = (JSONArray) frame.get("entities");

                        for (Object entityObject : entityArray) {
                            JSONObject entity = (JSONObject) entityObject;
                            //System.out.println(entity);
                            if (entity.containsKey("id") && entity.containsKey("x") && entity.containsKey("y")) {
                                int id = Integer.parseInt(entity.get("id").toString());
                                int x = Integer.parseInt(entity.get("x").toString());
                                int y = Integer.parseInt(entity.get("y").toString());
                                //System.out.println(id + ": " + x + ", " + y);

                                if (entities.containsKey(id)) {
                                    entities.get(id).xMap.put(time, x);
                                    entities.get(id).yMap.put(time, y);
                                }
                                else {
                                    Entity newE = new Entity(id);
                                    newE.xMap.put(time, x);
                                    newE.yMap.put(time, y);
                                    entities.put(id, newE);
                                }
                            } else {
                                System.out.println("Entity keys are wrong!");
                            }
                        }
                        //}

                    }

                    //if (time != -1 && entities.size() > 0) {
                    //    frames.put(time, entities);
                    //}

                }

            }



            //for (Long t: frames.keySet()) {
                synchronized (currentEntities) {
                    currentEntities.clear();
                    for (Entity e: entities.values()) {
                        currentEntities.add(e);
                        //System.out.println("Id: " + e.id + ", X: " + e.x + ", Y: " + e.y);
                    }
                }
                //break;
            //}


        }
        catch (Exception ex) {
            //System.out.println("HTTP GET ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }

    }
}
