import Steve.QuickMazeMaker;
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
    public static final int MAX_X = 21;
    public static final int MAX_Y = 17;

    public static int screen = 0;

    static HashSet<KeyCode> keysPressed = new HashSet<>();
    static final ArrayList<Entity> currentEntities = new ArrayList<>();
    static int viewportPosition = 0;
    public static final boolean fullscreen = true;

    // - - - - - - - - SERVER SETTINGS - - - - - - - - -  //
    public static final String serverAddress = "services.farnborough.ac.uk";
    //public static final String serverAddress = "localhost";
    //  - - - - - - - -  - - - - - - - -  - - - - - - - - //

    public static int[][] map = null;

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

        Image[] sprite = new Image[6];

        sprite[0] = new Image("resources/sprite1.png");
        sprite[1] = new Image("resources/sprite2.png");
        sprite[2] = new Image("resources/sprite3.png");
        sprite[3] = new Image("resources/sprite4.png");
        sprite[4] = new Image("resources/sprite5.png");
        sprite[5] = new Image("resources/sprite6.png");

        Image[] tile = new Image[256];

        tile[0] = new Image("resources/tile2.png");
        tile[128] = new Image("resources/tile4.png");
        tile[129] = new Image("resources/tile1.png");
        tile[130] = new Image("resources/tile3.png");

        new AnimationTimer() {
            @Override
            public void handle(long now) {

                for(KeyCode k : keysPressed) {

                    if (k == KeyCode.ESCAPE) System.exit(0);

                    int last_screen = screen;

                    if (k == KeyCode.TAB) screen = 0;

                    if (k == KeyCode.Q) screen = 1;
                    if (k == KeyCode.W) screen = 2;
                    if (k == KeyCode.E) screen = 3;
                    if (k == KeyCode.R) screen = 4;
                    if (k == KeyCode.T) screen = 5;

                    if (k == KeyCode.Y) screen = 6;
                    if (k == KeyCode.U) screen = 7;
                    if (k == KeyCode.I) screen = 8;
                    if (k == KeyCode.O) screen = 9;
                    if (k == KeyCode.P) screen = 10;

                    if (k == KeyCode.A) screen = 11;
                    if (k == KeyCode.S) screen = 12;
                    if (k == KeyCode.D) screen = 13;
                    if (k == KeyCode.F) screen = 14;
                    if (k == KeyCode.G) screen = 15;

                    if (k == KeyCode.H) screen = 16;
                    if (k == KeyCode.J) screen = 17;
                    if (k == KeyCode.K) screen = 18;
                    if (k == KeyCode.L) screen = 19;
                    if (k == KeyCode.SEMICOLON) screen = 20;

                    if (screen != last_screen) {
                        map = null;
                        last_screen = screen;
                        currentEntities.clear();
                    }

                }

                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

                if (map != null) {
                    for (int x = 0; x < MAX_X; x++) {
                        for (int y = 0; y < MAX_Y; y++) {
                            gc.drawImage(tile[map[x][y]], x * 64 - 32, y * 64 - 32);
                        }
                    }
                }

                long time = System.currentTimeMillis() >> 8;
                double offset = (System.currentTimeMillis() % 256) / 256.0;

                synchronized (currentEntities) {
                    for (Entity e : currentEntities) {

                        int x0 = -1;
                        int y0 = -1;
                        int x1 = -1;
                        int y1 = -1;

                        for (long t: e.xMap.keySet()) {
                            if (t == time) {
                                x0 = e.xMap.get(t);
                                y0 = e.yMap.get(t);
                            }
                            else if (t == time + 1) {
                                x1 = e.xMap.get(t);
                                y1 = e.yMap.get(t);
                            }
                        }

                        int imageNo = e.getType() - 1;

                        if (x0 != -1 && y0 != -1 && x1 != -1 && y1 != -1) {
                            int x = (int) (64.0 * (x0 + offset * (x1 - x0))) - 32;
                            int y = (int) (64.0 * (y0 + offset * (y1 - y0))) - 32;
                            gc.drawImage(sprite[imageNo], x - viewportPosition * WINDOW_WIDTH, y );
                        }

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
            url = new URL( "http://" + serverAddress + ":8081"
                                + "?index=" + clientTime
                                + "&map=" + (map == null ? "true" : "false")
                                + "&screen=" + screen);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            InputStream inputStream = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String inputjson="";
            while(br.ready()){
                inputjson = br.readLine();
            }

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(inputjson);
            JSONObject jsonObject = (JSONObject)obj;

            if (jsonObject.containsKey("map")) {

                map = QuickMazeMaker.emptyMap(MAX_X, MAX_Y);

                String mapString = jsonObject.get("map").toString();

                int x = 0;
                int y = 0;
                for (String value: mapString.split(",")) {
                    map[x][y] = Integer.parseInt(value);
                    x++;
                    if (x == MAX_X) {
                        x = 0;
                        y++;
                        if (y == MAX_Y) break;
                    }
                }

            }

            if (jsonObject.containsKey("frames")) {

                JSONArray frameArray = (JSONArray) jsonObject.get("frames");
                for (Object frameObject : frameArray) {
                    JSONObject frame = (JSONObject) frameObject;

                    long time = -1;

                    if (frame.containsKey("time") && frame.containsKey("position") && frame.containsKey("entities")) {

                        time = Long.parseLong(frame.get("time").toString());

                        viewportPosition = Integer.parseInt(frame.get("position").toString()) - 1;

                        JSONArray entityArray = (JSONArray) frame.get("entities");

                        for (Object entityObject : entityArray) {
                            JSONObject entity = (JSONObject) entityObject;
                            if (entity.containsKey("id") && entity.containsKey("x") && entity.containsKey("y")) {
                                int id = Integer.parseInt(entity.get("id").toString());
                                int type = Integer.parseInt(entity.get("type").toString());
                                int x = Integer.parseInt(entity.get("x").toString());
                                int y = Integer.parseInt(entity.get("y").toString());

                                if (entities.containsKey(id)) {
                                    entities.get(id).xMap.put(time, x);
                                    entities.get(id).yMap.put(time, y);
                                }
                                else {
                                    Entity newE = new Entity(id, type);
                                    newE.xMap.put(time, x);
                                    newE.yMap.put(time, y);
                                    entities.put(id, newE);
                                }
                            } else {
                                System.out.println("Entity keys are wrong!");
                            }
                        }
                    }
                }
            }

            synchronized (currentEntities) {
                currentEntities.clear();
                for (Entity e : entities.values()) {
                    currentEntities.add(e);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
