import Steve.QuickMazeMaker;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GameClient extends Application {

    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 1024;
    public static final int MAX_X = 21;
    public static final int MAX_Y = 17;

    public static int screen = 0;
    public static long maptimestamp = 0;
    public static int failCount = 0;

    static HashSet<KeyCode> keysPressed = new HashSet<>();
    static final ArrayList<ClientEntity> currentEntities = new ArrayList<>();
    static int viewportPosition = 0;

    public static String serverAddress = "localhost";
    public static boolean fullscreen = false;

    public static int[][] map = null;

    public static void main(String[] args) {
        try {
            String host = InetAddress.getLocalHost().getHostName().toLowerCase();
            if (host.contains("comp1-") && !host.contains("reg")) {
                serverAddress = "services.farnborough.ac.uk";
                fullscreen = true;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        launch(args);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void start(Stage stage) {

        Pane rootPane = new Pane();
        Scene scene = new Scene(rootPane);

        stage.setTitle("AMSALOS CLIENT");
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

        Image sprites = new Image("resources/all_sprites.png");
        Image tiles = new Image("resources/all_tiles.png");

        new AnimationTimer() {
            @Override
            public void handle(long now) {

                for(KeyCode k : keysPressed) {

                    if (k == KeyCode.ESCAPE) System.exit(0);

                    int last_screen = screen;

                    if (k == KeyCode.TAB) screen = 0;

                    if (keysPressed.contains(KeyCode.ALT)) {
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
                    }

                    if (screen != last_screen) {
                        map = null;
                        currentEntities.clear();
                    }

                }

                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

                if (map != null) {
                    for (int x = 0; x < MAX_X; x++) {
                        for (int y = 0; y < MAX_Y; y++) {

                            int value = map[x][y];
                            int baseTile = value % 256;

                            int column = baseTile % 16;
                            int row = baseTile / 16;

                            if (value >= 256) {

                                int quarterValues = value / 256;

                                int quarter1 = (quarterValues) % 16;
                                int quarter2 = (quarterValues >> 4) % 16;
                                int quarter3 = (quarterValues >> 8) % 16;
                                int quarter4 = (quarterValues >> 12) % 16;

                                column = (baseTile + quarter1) % 16;
                                gc.drawImage(tiles, column * 64, row * 64, 32, 32, x * 64 - 32, y * 64 - 32, 32, 32);

                                column = (baseTile + quarter2) % 16;
                                gc.drawImage(tiles, column * 64 + 32, row * 64, 32, 32, x * 64, y * 64 - 32, 32, 32);

                                column = (baseTile + quarter3) % 16;
                                gc.drawImage(tiles, column * 64, row * 64 + 32, 32, 32, x * 64 - 32, y * 64 , 32, 32);

                                column = (baseTile + quarter4) % 16;
                                gc.drawImage(tiles, column * 64 + 32, row * 64 + 32, 32, 32, x * 64 , y * 64, 32, 32);
                            }
                            else {
                                gc.drawImage(tiles, column * 64, row * 64, 64, 64, x * 64 - 32, y * 64 - 32, 64, 64);
                            }
                        }
                    }
                }

                long time = System.currentTimeMillis() >> 8;
                double offset = (System.currentTimeMillis() % 256) / 256.0;

                ColorAdjust dead = new ColorAdjust();
                dead.setSaturation(-1.0);
                dead.setBrightness(-0.5);

                DropShadow friendly = new DropShadow(20, Color.BLACK);

                Font nameFont = new Font( "Arial", 24);
                Font killFont = new Font( "Arial", 16);

                synchronized (currentEntities) {

                    for (int alive = 0; alive <= 1; alive++) {

                        for (ClientEntity e : currentEntities) {

                            if ((alive == 0 && e.getHealth() > 0) || (alive == 1 && e.getHealth() <= 0)) continue;

                            int x0 = -1;
                            int y0 = -1;
                            int x1 = -1;
                            int y1 = -1;

                            for (long t : e.xMap.keySet()) {
                                if (t == time) {
                                    x0 = e.xMap.get(t);
                                    y0 = e.yMap.get(t);
                                } else if (t == time + 1) {
                                    x1 = e.xMap.get(t);
                                    y1 = e.yMap.get(t);
                                }
                            }

                            if (x0 != -1 && y0 != -1 && x1 != -1 && y1 != -1) {
                                int x = (int) (64.0 * (x0 + offset * (x1 - x0))) - 32;
                                int y = (int) (64.0 * (y0 + offset * (y1 - y0))) - 32;
                                int column = (e.getType() - 1) % 16;
                                int row = (e.getType() - 1) / 16;

                                if (alive == 1) {
                                    if (!e.foe) gc.setEffect(friendly);
                                    gc.drawImage(sprites, column * 64, row * 64, 64, 64, x - viewportPosition * WINDOW_WIDTH, y, 64, 64);

                                    gc.setEffect(null);

                                    gc.setFill(Color.rgb(0, 255, 0, 0.5));
                                    gc.fillRect(x - viewportPosition * WINDOW_WIDTH, y - 20, 64 * e.getHealth(), 10);
                                    gc.setFill(Color.rgb(255, 0, 0, 0.5));
                                    gc.fillRect(x - viewportPosition * WINDOW_WIDTH + 64 * e.getHealth(), y - 20, 64 * (1 - e.getHealth()), 10);

                                    if (e.targetEntity != 0) {
                                        for (ClientEntity et : currentEntities) {
                                            if (et.getId() == e.targetEntity) {

                                                int x0b = -1;
                                                int y0b = -1;
                                                int x1b = -1;
                                                int y1b = -1;

                                                for (long t : et.xMap.keySet()) {
                                                    if (t == time) {
                                                        x0b = et.xMap.get(t);
                                                        y0b = et.yMap.get(t);
                                                    } else if (t == time + 1) {
                                                        x1b = et.xMap.get(t);
                                                        y1b = et.yMap.get(t);
                                                    }
                                                }

                                                if (x0b != -1 && y0b != -1 && x1b != -1 && y1b != -1) {

                                                    int xb = (int) (64.0 * (x0b + offset * (x1b - x0b))) - viewportPosition * WINDOW_WIDTH;
                                                    int yb = (int) (64.0 * (y0b + offset * (y1b - y0b)));

                                                    gc.setStroke(Color.rgb(255,0,0,0.5));
                                                    gc.setLineWidth(3);
                                                    gc.strokeLine(x + 32 - viewportPosition * WINDOW_WIDTH, y + 32, xb, yb);

                                                }

                                            }
                                        }
                                    }

                                }
                                else {
                                    gc.setEffect(dead);
                                    double alpha = 1 + e.getHealth();
                                    if (alpha < 0) alpha = 0;
                                    gc.setGlobalAlpha(alpha);
                                    gc.drawImage(sprites, column * 64, row * 64, 64, 64, x - viewportPosition * WINDOW_WIDTH, y, 64, 64);
                                    gc.setGlobalAlpha(1.0);
                                }

                                if (!e.foe && !e.getName().equals("")) {
                                    if (alive == 1) {
                                        gc.setFill(Color.rgb(255, 255, 255 ));
                                    } else {
                                        gc.setFill(Color.rgb(0, 0, 0));
                                    }

                                    gc.setTextAlign(TextAlignment.CENTER);
                                    gc.setTextBaseline(VPos.CENTER);
                                    gc.setFont(nameFont);
                                    gc.fillText(e.getName(), x + 32 - viewportPosition * WINDOW_WIDTH, y-32);
                                    gc.setFont(killFont);
                                    gc.fillText(e.getKills() + " kills", x + 32 - viewportPosition * WINDOW_WIDTH, y+64);
                                }

                                gc.setEffect(null);
                            }

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

    @SuppressWarnings("Duplicates")
    public static void requestPost(String query) {

        URL url;
        HttpURLConnection con;

        try {
            url = new URL( "http://" + serverAddress + ":8081?" + query);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            int responseCode = 0;
            try {
                responseCode = con.getResponseCode();
            }
            catch (ConnectException ce) {
                System.out.println("Unable to connect to server...");
            }
            if (responseCode != 200) {
                System.out.println("HTTP POST ERROR: " + responseCode);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("Duplicates")
    public static void getUpdate() {

        long clientTime = System.currentTimeMillis() >> 8;
        HashMap<Integer, ClientEntity> entities = new HashMap<>();

        URL url;
        HttpURLConnection con;

        try {
            url = new URL( "http://" + serverAddress + ":8081"
                                + "?index=" + clientTime
                                + "&mapTimeStamp=" + maptimestamp
                                + "&map=" + (map == null ? "true" : "false")
                                + "&screen=" + screen);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = 0;
            try {
                 responseCode = con.getResponseCode();
            }
            catch (ConnectException ce) {
                System.out.println("Unable to connect to server...");
                failCount++;
                if (failCount > 10) System.exit(-10);
            }

            if (responseCode == 200) {
                failCount = 0;
                InputStream inputStream = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String inputjson = "";
                while (br.ready()) {
                    inputjson = br.readLine();
                }

                JSONParser parser = new JSONParser();
                Object obj = parser.parse(inputjson);
                JSONObject jsonObject = (JSONObject) obj;

                if (jsonObject.containsKey("map")) {

                    map = QuickMazeMaker.emptyMap(MAX_X, MAX_Y);

                    String mapString = jsonObject.get("map").toString();

                    int x = 0;
                    int y = 0;
                    for (String value : mapString.split(",")) {
                        map[x][y] = Integer.parseInt(value);
                        x++;
                        if (x == MAX_X) {
                            x = 0;
                            y++;
                            if (y == MAX_Y) break;
                        }
                    }

                }

                if (jsonObject.containsKey("mapTimeStamp")) {
                    maptimestamp = Long.parseLong(jsonObject.get("mapTimeStamp").toString());
                    System.out.println("Recieved: " + maptimestamp);
                }

                if (jsonObject.containsKey("frames")) {

                    JSONArray frameArray = (JSONArray) jsonObject.get("frames");
                    for (Object frameObject : frameArray) {
                        JSONObject frame = (JSONObject) frameObject;

                        long time;

                        if (frame.containsKey("time") && frame.containsKey("position") && frame.containsKey("entities")) {

                            time = Long.parseLong(frame.get("time").toString());

                            viewportPosition = Integer.parseInt(frame.get("position").toString()) - 1;

                            JSONArray entityArray = (JSONArray) frame.get("entities");

                            for (Object entityObject : entityArray) {
                                JSONObject entity = (JSONObject) entityObject;

                                int id = Integer.parseInt(entity.get("i").toString());
                                int type = Integer.parseInt(entity.get("t").toString());
                                int x = Integer.parseInt(entity.get("x").toString());
                                int y = Integer.parseInt(entity.get("y").toString());
                                double health = Double.parseDouble(entity.get("h").toString());
                                int adjacentAttackers = Integer.parseInt(entity.get("a").toString());
                                boolean foe = Boolean.parseBoolean(entity.get("f").toString());
                                int target = Integer.parseInt(entity.get("z").toString());
                                String name = entity.get("n").toString();
                                int kills = Integer.parseInt(entity.get("k").toString());

                                if (entities.containsKey(id)) {
                                    entities.get(id).xMap.put(time, x);
                                    entities.get(id).yMap.put(time, y);
                                } else {
                                    ClientEntity newE = new ClientEntity(id, type, health, adjacentAttackers, foe, target);
                                    newE.xMap.put(time, x);
                                    newE.yMap.put(time, y);
                                    newE.setKills(kills);
                                    newE.setName(name);
                                    entities.put(id, newE);
                                }

                            }
                        }
                    }
                }

                synchronized (currentEntities) {
                    currentEntities.clear();
                    for (ClientEntity e : entities.values()) {
                        currentEntities.add(e);
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
