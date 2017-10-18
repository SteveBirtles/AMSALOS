import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GameClient extends Application {

    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 1024;
    public static final int MAX_X = 21;
    public static final int MAX_Y = 17;

    public static int screen = 0;
    public static boolean justUpdated =  false;
    //public static boolean enableHalfSpeed = false;

    static HashSet<KeyCode> keysPressed = new HashSet<>();
    static final ArrayList<ClientEntity> currentEntities = new ArrayList<>();

    public static String serverAddress = "localhost";
    public static boolean fullscreen = false;

    public static int[][] map = null;

    public static HashMap<Integer, Boolean> slowPoke = new HashMap<>();
    public static HashMap<Integer, Integer> lastX0 = new HashMap<>();
    public static HashMap<Integer, Integer> lastY0 = new HashMap<>();

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

                for (KeyCode k : keysPressed) {

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
                                gc.drawImage(tiles, column * 64, row * 64 + 32, 32, 32, x * 64 - 32, y * 64, 32, 32);

                                column = (baseTile + quarter4) % 16;
                                gc.drawImage(tiles, column * 64 + 32, row * 64 + 32, 32, 32, x * 64, y * 64, 32, 32);
                            } else {
                                gc.drawImage(tiles, column * 64, row * 64, 64, 64, x * 64 - 32, y * 64 - 32, 64, 64);
                            }
                        }
                    }
                }

                long time = System.currentTimeMillis() >> 8;

                ColorAdjust dead = new ColorAdjust();
                dead.setSaturation(-1.0);
                dead.setBrightness(-0.5);

                DropShadow friendly = new DropShadow(20, Color.BLACK);

                GaussianBlur timeyblur = new GaussianBlur(5);

                Font nameFont = new Font("Arial", 24);
                Font killFont = new Font("Arial", 16);

                synchronized (currentEntities) {

                    for (int fudge = 0; fudge <= 2; fudge++) {

                        int layer = 0;
                        switch (fudge) {
                            case 1:
                                layer = 2;
                                break;
                            case 2:
                                layer = 1;
                                break;
                        }

                        for (ClientEntity e : currentEntities) {

                            if (!e.status.containsKey(time)) continue;

                            if ((layer == 0 && e.status.get(time).health > 0) || (layer == 1 && e.status.get(time).health <= 0))
                                continue;

                            if (layer < 2 && e.getType() > 128) continue;
                            if (layer == 2 && e.getType() < 129) continue;

                            double offset = (System.currentTimeMillis() % 256) / 256.0;

                            int x1 = -1;
                            int y1 = -1;

                            int x0 = e.status.get(time).x;
                            int y0 = e.status.get(time).y;
                            if (e.status.containsKey(time + 1)) {
                                x1 = e.status.get(time + 1).x;
                                y1 = e.status.get(time + 1).y;
                            }

                            if (x1 != -1 && y1 != -1) {
                                int x = (int) (64.0 * (x0 + offset * (x1 - x0))) - 32;
                                int y = (int) (64.0 * (y0 + offset * (y1 - y0))) - 32;
                                int column = (e.getType() - 1) % 16;
                                int row = (e.getType() - 1) / 16;

                                if (layer == 2) {
                                    gc.drawImage(sprites, column * 64, row * 64, 64, 64, x - ClientShared.viewportPosition * WINDOW_WIDTH, y, 64, 64);
                                } else if (layer == 1) {
                                    if (!e.getFoe()) gc.setEffect(friendly);
                                    gc.drawImage(sprites, column * 64, row * 64, 64, 64, x - ClientShared.viewportPosition * WINDOW_WIDTH, y, 64, 64);
                                    gc.setEffect(null);

                                    gc.setFill(Color.rgb(0, 255, 0, 0.5));
                                    gc.fillRect(x - ClientShared.viewportPosition * WINDOW_WIDTH, y - 20, 64 * e.status.get(time).health, 10);
                                    gc.setFill(Color.rgb(255, 0, 0, 0.5));
                                    gc.fillRect(x - ClientShared.viewportPosition * WINDOW_WIDTH + 64 * e.status.get(time).health, y - 20, 64 * (1 - e.status.get(time).health), 10);

                                } else {
                                    gc.setEffect(dead);
                                    double alpha = 1 + e.status.get(time).health;
                                    if (alpha < 0) alpha = 0;
                                    gc.setGlobalAlpha(alpha);
                                    gc.drawImage(sprites, column * 64, row * 64, 64, 64, x - ClientShared.viewportPosition * WINDOW_WIDTH, y, 64, 64);
                                    gc.setGlobalAlpha(1.0);
                                }

                                //gc.setFill(Color.rgb(0, 0, 64));
                                //gc.setFont(nameFont);
                                //gc.fillText(Long.toString(time), x + 32 - ClientShared.viewportPosition * WINDOW_WIDTH, y - 20);

                                if (layer < 2 && !e.getFoe() && !e.getName().equals("")) {
                                    if (layer == 1) {
                                        gc.setFill(Color.rgb(255, 255, 255));
                                    } else {
                                        gc.setFill(Color.rgb(0, 0, 0));
                                    }

                                    gc.setTextAlign(TextAlignment.CENTER);
                                    gc.setTextBaseline(VPos.CENTER);
                                    gc.setFont(nameFont);
                                    gc.fillText(e.getName(), x + 32 - ClientShared.viewportPosition * WINDOW_WIDTH, y - 16 * (1 + layer));
                                    gc.setFont(killFont);
                                    gc.fillText("Score: " + e.status.get(time).score, x + 32 - ClientShared.viewportPosition * WINDOW_WIDTH, y + 72);
                                }

                                gc.setEffect(null);
                            }

                            if (layer == 1) {

                                for (long timeywimey : e.status.keySet()) {

                                    offset = (System.currentTimeMillis() % 256) / 256.0;

                                    x1 = -1;
                                    y1 = -1;

                                    x0 = e.status.get(timeywimey).x;
                                    y0 = e.status.get(timeywimey).y;
                                    if (e.status.containsKey(timeywimey + 1)) {
                                        x1 = e.status.get(timeywimey + 1).x;
                                        y1 = e.status.get(timeywimey + 1).y;
                                    }

                                    if (x1 != -1 && y1 != -1) {
                                        int x = (int) (64.0 * (x0 + offset * (x1 - x0))) - 32;
                                        int y = (int) (64.0 * (y0 + offset * (y1 - y0))) - 32;
                                        int column = (e.getType() - 1) % 16;
                                        int row = (e.getType() - 1) / 16;

                                        gc.setEffect(timeyblur);
                                        gc.drawImage(sprites, column * 64, row * 64, 64, 64, x - ClientShared.viewportPosition * WINDOW_WIDTH, y, 64, 64);
                                        gc.setEffect(null);

                                    }

                                }
                            }
                        }


                        if (justUpdated) {
                            justUpdated = false;
                            //gc.setFill(Color.rgb(255,255,255,0.5));
                            //gc.fillRect(0, 0, 32, 32);
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

    private static void getUpdate() {
        map = ClientShared.getUpdate(serverAddress, map, screen, false, currentEntities);
        justUpdated = true;

        /*for (ClientEntity e : currentEntities) {
            if (e.getPause() > 0) {
                slowPoke.put(e.getId(), true);
            } else if(e.getHealth() <= 0 || e.getPause() == 0) {
                slowPoke.put(e.getId(), false);
            }
        }*/

    }

}
