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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;

public class GamePlayer extends Application {

    public static final int WINDOW_WIDTH = 1920;
    public static final int WINDOW_HEIGHT = 1080;
    public static final int MAX_X = 401;
    public static final int MAX_Y = 17;
    public static final int SPRITE_COUNT = 16;

    static HashSet<KeyCode> keysPressed = new HashSet<>();
    static final ArrayList<ClientEntity> currentEntities = new ArrayList<>();

    public static String serverAddress = "localhost";
    public static boolean fullscreen = false;

    public static int[][] map = null;

    public static ImageView selectedEntityImageView;
    public static int selectedEntity = 1;

    public static void main(String[] args) {
        fullscreen = true;
        try {
            String host = InetAddress.getLocalHost().getHostName().toLowerCase();
            if (host.equals("comp1-reg")) {
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
        int aiType = (selectedEntity + 1) % 4;
        ClientShared.requestPost(serverAddress, "add=" + selectedEntity + "&screen=" + screen + "&aitype=" + aiType);
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
        borderPane.setCenter(entityView);
        borderPane.setAlignment(entityChooser, Pos.BOTTOM_CENTER);


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

                        if (e.getHealth() <= 0) continue;

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

                            if (e.getAdjacentAttackers() > 0) {
                                gc.setFill(Color.RED);
                            }
                            else {
                                gc.setFill(Color.WHITE);
                            }

                            gc.fillRect(x, y, 4, 4);
                        }

                    }
                }

            }
        }.start();

        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(256),
                ae -> map = ClientShared.getUpdate(serverAddress, map, 0, currentEntities)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

    }

}

