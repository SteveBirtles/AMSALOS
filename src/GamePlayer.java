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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;

public class GamePlayer extends Application {

    public static final int WINDOW_WIDTH = 1920;
    public static final int WINDOW_HEIGHT = 1080;
    public static final int MAX_X = 401;
    public static final int MAX_Y = 17;
    public static final int SPRITE_COUNT = 4;

    static HashSet<KeyCode> keysPressed = new HashSet<>();
    static final ArrayList<ClientEntity> currentEntities = new ArrayList<>();

    public static String serverAddress = "localhost";
    public static boolean fullscreen = false;

    public static int[][] map = null;

    public static ImageView selectedEntityImageView;
    public static int selectedEntity = 1;

    public static int selectedSkill = 0;
    public static String[] skillName = {"Resilience", "Offensive", "Defensive", "Greed"};
    public static Button[] skillButtons;

    public static TextField nameBox;

    public static void main(String[] args) {
        fullscreen = true;
        try {
            String host = InetAddress.getLocalHost().getHostName().toLowerCase();
            if (host.equals("comp1-reg")) {
                serverAddress = "services.farnborough.ac.uk";
                //fullscreen = false;
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
        String playerName = null;
        playerName = nameBox.getText();
        playerName = playerName.replace(" ", "_");
        String postString = "add=" + selectedEntity + "&screen=" + screen + "&aitype=4&name=" + playerName + "&skill=" + selectedSkill;
        System.out.println(postString);
        ClientShared.requestPost(serverAddress, postString);
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

        HBox nameChooser = new HBox(32);
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
                "-fx-text-fill: darkgrey;");
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

        selectedEntityImageView = new ImageView();
        selectedEntityImageView.setImage(sprites);
        selectedEntityImageView.setFitWidth(384);
        selectedEntityImageView.setFitHeight(384);
        DropShadow ds = new DropShadow( 64, Color.WHITE );
        selectedEntityImageView.setEffect(ds);
        setSelectedEntity(1);
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
                    "-fx-background-color: lightslategray;");
            final int entityNumber = i + 1;
            entityButton.setOnAction((ActionEvent e) -> setSelectedEntity(entityNumber));
            entityChooser.getChildren().add(entityButton);
        }
        entityChooser.setMaxWidth(1604);
        entityChooser.setAlignment(Pos.CENTER);
        entityView.getChildren().add(entityChooser);

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
                            ClientShared.requestPost(serverAddress, "reset=1");
                        }
                        else if (k == KeyCode.X) {
                            ClientShared.requestPost(serverAddress, "reset=2");
                        }
                        else if (k == KeyCode.C) {
                            ClientShared.requestPost(serverAddress, "reset=3");
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

                        if (e.status.get(time).adjacentAttackers > 0) {
                            gc.setFill(Color.RED);
                        }
                        else if (e.getType() > 128) {
                            gc.setFill(Color.DARKCYAN);
                        }
                        else if (e.getFoe()) {
                            gc.setFill(Color.DARKGOLDENROD);
                        }
                        else {
                            gc.setFill(Color.LIMEGREEN);
                        }

                        gc.fillRect(x, y, 4, 4);

                    }
                }

            }
        }.start();

        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(256),
                ae -> map = ClientShared.getUpdate(serverAddress, map, 0, true, currentEntities)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

    }

}


