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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

public class GamePlayer extends Application {

    private static class Score implements Comparator<Score> {
        public int score;
        public String name;
        public boolean alive;

        public Score() {}

        public Score(int score, String name, boolean alive) {
            this.score = score;
            this.name = name;
            this.alive = alive;
        }

        @Override
        public String toString() {
            return score + ": " + name;
        }

        @Override
        public int compare(Score s1, Score s2) {
            return s2.score - s1.score;
        }
    }

    public static final int WINDOW_WIDTH = 1920;
    public static final int WINDOW_HEIGHT = 1080;
    public static final int MAX_X = 401;
    public static final int MAX_Y = 17;
    public static final int SPRITE_COUNT = 5;

    static HashSet<KeyCode> keysPressed = new HashSet<>();
    static final ArrayList<ClientEntity> currentEntities = new ArrayList<>();

    public static String serverAddress = "localhost";
    public static boolean fullscreen = false;

    public static int[][] map = null;

    public static ImageView selectedEntityImageView;
    public static int selectedEntity = 0;

    public static int selectedSkill = 0;
    public static String[] skillName = {"Resilience", "Offensive", "Defensive", "Greed"};
    public static Button[] skillButtons;

    public static TextField nameBox;
    public static Label highScoreLabel[];
    public static Label statsLabel[];

    public static void main(String[] args) {
        fullscreen = true;
        if (args.length > 0) serverAddress = args[0];
        try {
            String host = InetAddress.getLocalHost().getHostName().toLowerCase();
            if (host.equals("Comp1-Reg")) {
                serverAddress = "172.16.0.123";
                //fullscreen = false;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        launch(args);
    }

    public void setSelectedEntity(int number) {
        selectedEntity = number;
        if (selectedEntity == 5) {
            Random rnd = new Random(System.currentTimeMillis());
            selectedEntity = rnd.nextInt(4) + 1;
            setSelectedSkill(rnd.nextInt(4) + 1);
            nameBox.setText(Steve.QuickNameMaker.next(rnd));
        }
        if (selectedEntity == 0) {
            selectedEntityImageView.setViewport(new Rectangle2D(15 * 64, 0, 64, 64));
        } else {
            selectedEntityImageView.setViewport(new Rectangle2D((selectedEntity - 1) * 64, 0, 64, 64));
        }
    }

    public void setSelectedSkill(int number) {
        selectedSkill = number;
        for (int i = 0; i < 4; i++) {
            skillButtons[i].setStyle("-fx-border-color: transparent;\n" +
                    "-fx-border-width: 0;\n" +
                    "-fx-background-radius: 0;\n" +
                    "-fx-background-color: " + (i == number - 1 ? "gold;" : "silver;") +
                    "-fx-font-size: 24;" +
                    "-fx-font-family: monospace;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: black;");
        }
    }

    public void addEntity(int screen) {
        if (nameBox.getText().equals("") || selectedEntity == 0 || selectedSkill == 0) return;
        String playerName = null;
        playerName = nameBox.getText();
        playerName = playerName.replace(" ", "_");
        String postString = "add=" + selectedEntity + "&screen=" + screen + "&aitype=4&name=" + playerName + "&skill=" + selectedSkill;
        System.out.println(postString);
        ClientShared.requestPost(serverAddress, postString);
        nameBox.setText("");
        setSelectedEntity(0);
        setSelectedSkill(0);
    }

    @SuppressWarnings("Duplicates")
    public void start(Stage stage) {

        Pane rootPane = new Pane();
        Scene scene = new Scene(rootPane);

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
        //Image tiles = new Image("resources/all_tiles.png");

        BorderPane borderPane = new BorderPane();
        borderPane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        borderPane.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;" +
                "-fx-background-color: white");

        Image logo = new Image("resources/Amsalos.png");
        ImageView logoView = new ImageView(logo);
        borderPane.setTop(logoView);
        borderPane.setAlignment(logoView, Pos.TOP_CENTER);

        Canvas miniMapCanvas = new Canvas();
        miniMapCanvas.setWidth(1604);
        miniMapCanvas.setHeight(68);
        miniMapCanvas.setLayoutX(158);
        miniMapCanvas.setLayoutY(882);

        HBox screenButtons = new HBox();
        screenButtons.setSpacing(10);
        screenButtons.setPadding(new Insets(5,5,0,10));
        for (int i = 1; i <= 20; i++) {
            Button screenButton = new Button(Integer.toString(i));
            screenButton.setPrefSize(70, 30);
            screenButton.setStyle(  "-fx-border-color: transparent;\n" +
                                    "-fx-border-width: 0;\n" +
                                    "-fx-background-radius: 0;\n" +
                                    "-fx-background-color: navy;" +
                                    "-fx-font-size: 24;" +
                                    "-fx-font-family: monospace;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-text-fill: white;");

            final int screenNumber = i;
            screenButton.setOnAction((ActionEvent ae) -> addEntity(screenNumber));
            screenButtons.getChildren().add(screenButton);
        }

        highScoreLabel = new Label[11];

        highScoreLabel[0] = new Label("High Scores:");
        highScoreLabel[1] = new Label("...");
        highScoreLabel[2] = new Label("...");
        highScoreLabel[3] = new Label("...");
        highScoreLabel[4] = new Label("...");
        highScoreLabel[5] = new Label("...");
        highScoreLabel[6] = new Label("...");
        highScoreLabel[7] = new Label("...");
        highScoreLabel[8] = new Label("...");
        highScoreLabel[9] = new Label("...");
        highScoreLabel[10] = new Label("...");

        VBox highScores = new VBox(32);
        highScores.setPadding(new Insets(32));
        highScores.setAlignment(Pos.TOP_RIGHT);

        highScoreLabel[0].setStyle("-fx-background-color: transparent;" +
                "-fx-font-size: 24;" +
                "-fx-font-family: monospace;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;");

        for (int s = 0; s < 11; s++) {
            highScoreLabel[s].setTextAlignment(TextAlignment.RIGHT);
            highScores.getChildren().add(highScoreLabel[s]);
        }

        highScores.setStyle("-fx-background-color: black");

        borderPane.setRight(highScores);
        borderPane.setAlignment(highScores, Pos.TOP_RIGHT);

        VBox worldStats = new VBox(32);
        worldStats.setPadding(new Insets(32));

        statsLabel = new Label[11];

        statsLabel[0] = new Label("World Statistics:");
        statsLabel[1] = new Label("Player count");
        statsLabel[2] = new Label("...");
        statsLabel[3] = new Label("Enemy count");
        statsLabel[4] = new Label("...");
        statsLabel[5] = new Label("Treasure count");
        statsLabel[6] = new Label("...");
        statsLabel[7] = new Label("World age");
        statsLabel[8] = new Label("...");
        statsLabel[9] = new Label("World seed");
        statsLabel[10] = new Label("...");

        for (int s = 0; s < 11; s++) {
            statsLabel[s].setPrefWidth(300);
            String c = "white";
            if (s > 0) {
                if (s % 2 == 0) { c = "darkred"; }
                else { c = "red"; }
            }
            statsLabel[s].setStyle("-fx-background-color: transparent;" +
                    "-fx-font-size: 24;" +
                    "-fx-font-family: monospace;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: " + c + ";");
            worldStats.getChildren().add(statsLabel[s]);
        }

        worldStats.setStyle("-fx-background-color: black");
        borderPane.setLeft(worldStats);
        borderPane.setAlignment(worldStats, Pos.TOP_LEFT);


        VBox miniMapHBox = new VBox();
        miniMapHBox.setSpacing(5);
        miniMapHBox.getChildren().add(screenButtons);
        miniMapHBox.getChildren().add(miniMapCanvas);
        miniMapHBox.setMaxWidth(1604);
        borderPane.setBottom(miniMapHBox);
        borderPane.setAlignment(miniMapHBox, Pos.BOTTOM_CENTER);

        VBox entityView = new VBox();
        entityView.setSpacing(32);
        entityView.setStyle("-fx-background-color: black");
        entityView.setPadding(new Insets(32));
        entityView.setAlignment(Pos.BOTTOM_CENTER);

        selectedEntityImageView = new ImageView();
        selectedEntityImageView.setImage(sprites);
        selectedEntityImageView.setFitWidth(384);
        selectedEntityImageView.setFitHeight(384);
        DropShadow ds = new DropShadow( 64, Color.WHITE );
        selectedEntityImageView.setEffect(ds);
        setSelectedEntity(0);
        entityView.getChildren().add(selectedEntityImageView);

        HBox entityChooser = new HBox();
        entityChooser.setSpacing(10);
        for (int i = 0; i < SPRITE_COUNT; i++) {
            Button entityButton = new Button();
            ImageView entityImage = new ImageView();
            entityImage.setImage(sprites);
            Rectangle2D rect = new Rectangle2D(i*64,0,64,64);
            entityImage.setViewport(rect);
            entityButton.setGraphic(entityImage);
            entityButton.setStyle(  "-fx-border-color: transparent;\n" +
                    "-fx-border-width: 0;\n" +
                    "-fx-background-radius: 0;\n" +
                    "-fx-background-color: " + (i == 4 ? "#000022" : "lightslategray" )+ ";");
            final int entityNumber = i + 1;
            entityButton.setOnAction((ActionEvent e) -> setSelectedEntity(entityNumber));
            entityChooser.getChildren().add(entityButton);
        }
        entityChooser.setMaxWidth(1604);
        entityChooser.setAlignment(Pos.CENTER);
        entityView.getChildren().add(entityChooser);

        HBox nameChooser = new HBox();
        nameChooser.setPrefWidth(700);
        nameChooser.setAlignment(Pos.CENTER);
        Label nameLabel = new Label("Player name:");
        nameLabel.setPrefWidth(200);
        nameLabel.setTextAlignment(TextAlignment.RIGHT);
        nameLabel.setStyle("-fx-border-color: transparent;\n" +
                "-fx-border-width: 0;\n" +
                "-fx-background-radius: 0;\n" +
                "-fx-background-color: black;" +
                "-fx-font-size: 24;" +
                "-fx-font-family: monospace;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;");
        nameChooser.getChildren().add(nameLabel);

        nameBox = new TextField();
        nameBox.setPrefWidth(400);
        nameBox.setStyle("-fx-border-color: transparent;\n" +
                "-fx-border-width: 0;\n" +
                "-fx-background-radius: 0;\n" +
                "-fx-background-color: navy;" +
                "-fx-font-size: 24;" +
                "-fx-font-family: monospace;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;");
        nameChooser.getChildren().add(nameBox);
        entityView.getChildren().add(nameChooser);

        HBox skillChooser = new HBox();
        skillChooser.setSpacing(10);
        skillButtons = new Button[4];
        for (int i = 0; i < 4; i++) {
            skillButtons[i] = new Button(skillName[i]);
            skillButtons[i].setPrefWidth(200);
            skillButtons[i].setStyle("-fx-border-color: transparent;\n" +
                    "-fx-border-width: 0;\n" +
                    "-fx-background-radius: 0;\n" +
                    "-fx-background-color: " + (i == selectedSkill - 1 ? "gold;" : "silver;") +
                    "-fx-font-size: 24;" +
                    "-fx-font-family: monospace;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: black;");

            final int skillNumber = i + 1;
            skillButtons[i].setOnAction((ActionEvent e) -> setSelectedSkill(skillNumber));
            skillChooser.getChildren().add(skillButtons[i]);
        }
        skillChooser.setMaxWidth(1604);
        skillChooser.setAlignment(Pos.CENTER);
        entityView.getChildren().add(skillChooser);

        borderPane.setCenter(entityView);
        borderPane.setAlignment(entityView, Pos.BOTTOM_CENTER);

        rootPane.getChildren().add(borderPane);

        GraphicsContext gc = miniMapCanvas.getGraphicsContext2D();

        new AnimationTimer() {
            @Override
            public void handle(long now) {

                for(KeyCode k : keysPressed) {

                    if (k == KeyCode.ESCAPE) System.exit(0);

                    if (keysPressed.contains(KeyCode.CONTROL) && keysPressed.contains(KeyCode.ALT)) {
                        if (k == KeyCode.Z) {
                            //ClientShared.requestPost(serverAddress, "reset=1");
                        }
                        else if (k == KeyCode.X) {
                            //ClientShared.requestPost(serverAddress, "reset=2");
                        }
                        else if (k == KeyCode.C) {
                            //ClientShared.requestPost(serverAddress, "reset=3");
                        }
                    }
                }

                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

                if (map != null) {
                    for (int x = 0; x < MAX_X; x++) {
                        for (int y = 0; y < MAX_Y; y++) {
                            if (map[x][y]%256 < 128) {
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
                    for (ClientEntity e : currentEntities) {

                        if (!e.status.containsKey(time)) continue;
                        if (!e.status.containsKey(time + 1)) continue;

                        if (e.status.get(time).health <= 0) continue;

                        int x0 = e.status.get(time).x;
                        int y0 = e.status.get(time).y;

                        int x1 = e.status.get(time + 1).x;
                        int y1 = e.status.get(time + 1).y;

                        int x = (int) (4.0 * (x0 + offset * (x1 - x0)));
                        int y = (int) (4.0 * (y0 + offset * (y1 - y0)));

                        if (e.getType() > 128) {
                            gc.setFill(Color.DARKGREEN);
                        }
                        else if (e.getFoe()) {
                            gc.setFill(Color.RED);
                        }
                        else {
                            gc.setFill(Color.WHITE);
                        }

                        gc.fillRect(x, y, 4, 4);

                    }
                }

            }
        }.start();

        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(256),
                ae -> update()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

    }

    public static void update() {

        map = ClientShared.getUpdate(serverAddress, map, 0, true, currentEntities);
        ArrayList<Score> currentHighScores = new ArrayList<>();
        for (ClientEntity e: currentEntities) {
            if (!e.getName().equals("")) {
                long last = 0;
                for (long l : e.status.keySet()) {
                    if (l > last) last = l;
                }
                currentHighScores.add(new Score(e.status.get(last).score, e.getName(), e.status.get(last).health > 0));
            }
        }
        currentHighScores.sort(new Score());

        for (int s = 1; s <= 10; s++) {
            if (s <= currentHighScores.size()) {
                if (currentHighScores.get(s - 1).alive) {
                    highScoreLabel[s].setStyle("-fx-background-color: transparent;" +
                            "-fx-font-size: 24;" +
                            "-fx-font-family: Arial;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: yellow;");
                } else {
                    highScoreLabel[s].setStyle("-fx-background-color: transparent;" +
                            "-fx-font-size: 24;" +
                            "-fx-font-family: Arial;" +
                            "-fx-font-weight: normal;" +
                            "-fx-text-fill: darkgoldenrod;");
                }
                highScoreLabel[s].setText(currentHighScores.get(s - 1).toString());
                highScoreLabel[s].setMaxWidth(250);
            }
            else {
                highScoreLabel[s].setText("");
            }
        }

        int enemyCount = 0;
        int treasureCount = 0;
        int playerCount = 0;

        for (ClientEntity e: currentEntities) {
            long last = 0;
            for (long l : e.status.keySet()) {
                if (l > last) last = l;
            }
            if (e.getType() > 128) {
                treasureCount++;
            } else if (e.status.get(last).health > 0) {
                if (e.getType() < 16) {
                    playerCount++;
                } else {
                    enemyCount++;
                }
            }
        }

        long mapLifetime = (System.currentTimeMillis() >> 8) - ClientShared.mapTimeStamp;

        statsLabel[2].setText(Integer.toString(playerCount));
        statsLabel[4].setText(Integer.toString(enemyCount));
        statsLabel[6].setText(Integer.toString(treasureCount));
        int seconds = Math.floorDiv((int) mapLifetime, 4) % 60;
        int minutes = Math.floorDiv((int) mapLifetime, 60*4);
        statsLabel[8].setText(Integer.toString(minutes) + (seconds < 10 ? ":0" : ":") + Integer.toString(seconds));
        statsLabel[10].setText(Long.toString(ClientShared.mapTimeStamp));

    }

}


