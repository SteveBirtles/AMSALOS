import Steve.QuickMazeMaker;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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

public class GamePlayer extends Application {

    public static final int WINDOW_WIDTH = 1920;
    public static final int WINDOW_HEIGHT = 1080;
    public static final int MAX_X = 401;
    public static final int MAX_Y = 17;

    //public static int screen = 0;
    public static long maptimestamp = 0;
    public static int failCount = 0;

    static HashSet<KeyCode> keysPressed = new HashSet<>();
    static final ArrayList<Entity> currentEntities = new ArrayList<>();
    //static int viewportPosition = 0;

    public static String serverAddress = "localhost";
    public static boolean fullscreen = false;

    public static int[][] map = null;

    public static ImageView selectedEntityImageView;
    public static int selectedEntity = 1;

    @SuppressWarnings("Duplicates")
    public static void main(String[] args) {
        fullscreen = true;
        try {
            String host = InetAddress.getLocalHost().getHostName().toLowerCase();
            if (host.contains("comp1-") && !host.contains("reg")) {
                serverAddress = "services.farnborough.ac.uk";
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        launch(args);
    }

    public void setSelectedEntity(int number) {
        selectedEntity = number;
        selectedEntityImageView.setViewport(new Rectangle2D((selectedEntity-1)*64,0,64,64));
    }

    public void addEntity(int screen) {
        requestPost("add=" + selectedEntity + "&screen=" + screen);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void start(Stage primaryStage) {

        Pane rootPane = new Pane();
        Scene scene = new Scene(rootPane);

        Stage stage = new Stage();
        stage.setTitle("AMSALOS PLAYER");
        stage.setResizable(false);
        stage.setFullScreen(fullscreen);
        stage.setScene(scene);
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.setOnCloseRequest((WindowEvent we) -> System.exit(0));
        stage.show();

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> keysPressed.add(event.getCode()));
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> keysPressed.remove(event.getCode()));

        Image sprites = new Image("resources/all_sprites.png");
        Image tiles = new Image("resources/all_tiles.png");

        BorderPane borderPane = new BorderPane();
        borderPane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        borderPane.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;" +
                "-fx-background-color: white");

        Canvas miniMapCanvas = new Canvas();
        miniMapCanvas.setWidth(1604);
        miniMapCanvas.setHeight(68);
        miniMapCanvas.setLayoutX(158);
        miniMapCanvas.setLayoutY(882);

        HBox screenButtons = new HBox();
        screenButtons.setSpacing(10);
        screenButtons.setPadding(new Insets(5));
        for (int i = 1; i <= 20; i++) {
            Button screenButton = new Button(Integer.toString(i));
            screenButton.setPrefSize(70, 40);
            screenButton.setFont(new Font(24));
            final int screenNumber = i;
            screenButton.setOnAction((ActionEvent ae) -> addEntity(screenNumber));
            screenButtons.getChildren().add(screenButton);
        }

        VBox miniMapHBox = new VBox();
        miniMapHBox.setSpacing(10);
        miniMapHBox.getChildren().add(screenButtons);
        miniMapHBox.getChildren().add(miniMapCanvas);
        miniMapHBox.setMaxWidth(1604);
            borderPane.setBottom(miniMapHBox);
        borderPane.setAlignment(miniMapHBox, Pos.BOTTOM_CENTER);

        VBox entityView = new VBox();
        entityView.setSpacing(32);
        entityView.setPadding(new Insets(32));
        entityView.setAlignment(Pos.BOTTOM_CENTER);

        selectedEntityImageView = new ImageView();
        selectedEntityImageView.setImage(sprites);
        selectedEntityImageView.setFitWidth(256);
        selectedEntityImageView.setFitHeight(256);
        DropShadow ds = new DropShadow( 50, Color.BLACK );
        selectedEntityImageView.setEffect(ds);
        setSelectedEntity(1);
        entityView.getChildren().add(selectedEntityImageView);

        HBox entityChooser = new HBox();
        entityChooser.setSpacing(10);
        for (int i = 0; i < 9; i++) {
            Button entityButton = new Button();
            ImageView entityImage = new ImageView();
            entityImage.setImage(sprites);
            Rectangle2D rect = new Rectangle2D(i*64,0,64,64);
            entityImage.setViewport(rect);
            entityButton.setGraphic(entityImage);
            final int entityNumber = i + 1;
            entityButton.setOnAction((ActionEvent e) -> setSelectedEntity(entityNumber));
            entityChooser.getChildren().add(entityButton);
        }
        entityChooser.setMaxWidth(1604);
        entityChooser.setAlignment(Pos.CENTER);
        entityView.getChildren().add(entityChooser);
        borderPane.setCenter(entityView);
        borderPane.setAlignment(entityChooser, Pos.BOTTOM_CENTER);


        rootPane.getChildren().add(borderPane);

        GraphicsContext gc = miniMapCanvas.getGraphicsContext2D();


        new AnimationTimer() {
            @Override
            public void handle(long now) {

                for(KeyCode k : keysPressed) {

                    if (k == KeyCode.ESCAPE) System.exit(0);

                    if (k == KeyCode.X && keysPressed.contains(KeyCode.CONTROL) && keysPressed.contains(KeyCode.ALT)) requestPost("reset=true");

                    /*if (keysPressed.contains(KeyCode.ALT)) {
                        if (k == KeyCode.Q) requestPost("add=1");
                        if (k == KeyCode.W) requestPost("add=2");
                        if (k == KeyCode.E) requestPost("add=3");
                        if (k == KeyCode.R) requestPost("add=4");
                        if (k == KeyCode.T) requestPost("add=5");
                        if (k == KeyCode.Y) requestPost("add=6");
                        if (k == KeyCode.U) requestPost("add=7");
                        if (k == KeyCode.I) requestPost("add=8");
                        if (k == KeyCode.O) requestPost("add=9");
                        if (k == KeyCode.P) requestPost("add=10");
                        if (k == KeyCode.A) requestPost("add=11");
                        if (k == KeyCode.S) requestPost("add=12");
                        if (k == KeyCode.D) requestPost("add=13");
                        if (k == KeyCode.F) requestPost("add=14");
                        if (k == KeyCode.G) requestPost("add=15");
                        if (k == KeyCode.H) requestPost("add=16");
                        if (k == KeyCode.J) requestPost("add=17");
                        if (k == KeyCode.K) requestPost("add=18");
                        if (k == KeyCode.L) requestPost("add=19");
                        if (k == KeyCode.SEMICOLON) requestPost("add=20");
                    }*/

                }

                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

                if (map != null) {
                    for (int x = 0; x < MAX_X; x++) {
                        for (int y = 0; y < MAX_Y; y++) {
                            if (map[x][y] < 128) {
                                gc.setFill(Color.BLACK);
                            }
                            else {
                                gc.setFill(Color.BLUE);
                            }
                            gc.fillRect( x*4, y*4, 4, 4);
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

                        if (x0 != -1 && y0 != -1 && x1 != -1 && y1 != -1) {
                            int x = (int) (4.0 * (x0 + offset * (x1 - x0)));
                            int y = (int) (4.0 * (y0 + offset * (y1 - y0)));

                            gc.setFill(Color.WHITE);
                            gc.fillRect(x, y, 4, 4);
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
        HashMap<Integer, Entity> entities = new HashMap<>();

        URL url;
        HttpURLConnection con;

        try {
            url = new URL( "http://" + serverAddress + ":8081"
                    + "?index=" + clientTime
                    + "&player=true"
                    + "&maptimestamp=" + maptimestamp
                    + "&map=" + (map == null ? "true" : "false"));
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

                if (jsonObject.containsKey("maptimestamp")) {
                    maptimestamp = Long.parseLong(jsonObject.get("maptimestamp").toString());
                    //System.out.println("Recieved: " + maptimestamp);
                }

                if (jsonObject.containsKey("frames")) {

                    JSONArray frameArray = (JSONArray) jsonObject.get("frames");
                    for (Object frameObject : frameArray) {
                        JSONObject frame = (JSONObject) frameObject;

                        long time = -1;

                        if (frame.containsKey("time") && frame.containsKey("position") && frame.containsKey("entities")) {

                            time = Long.parseLong(frame.get("time").toString());

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
                                    } else {
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
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}

