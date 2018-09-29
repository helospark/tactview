package com.helospark.tactview.ui.javafx;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.LightDi;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.LightDiContextConfiguration;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.jpaplugin.JnaLightDiPlugin;
import com.helospark.tactview.ui.javafx.uicomponents.UiTimeline;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class JavaFXUiMain extends Application {
    public static final int W = 320; // canvas dimensions.
    public static final int H = 260;

    static LightDiContext lightDi;

    private static UiCommandInterpreterService commandInterpreter;
    private static PlaybackController playbackController;

    static BufferedImage bufferedImage;

    int frames = 0;
    double zoom = 1.0;
    long currentTime = System.currentTimeMillis();
    static int second = 0;

    static List<BufferedImage> images = new ArrayList<>(30);
    static List<BufferedImage> backBuffer = new ArrayList<>(30);
    volatile static boolean backBufferReady = false;

    static UiTimelineManager uiTimelineManager;
    private static Line line;
    private static Canvas canvas;
    private static Label videoTimestampLabel;
    static UiTimeline uiTimeline;

    @Override
    public void start(Stage stage) throws IOException {
        //        updateImage(stage);
        NotificationPane pane = new NotificationPane();
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 650, 550, Color.GREY);

        final KeyCombination keyComb1 = new KeyCodeCombination(KeyCode.Z,
                KeyCombination.CONTROL_DOWN);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (keyComb1.match(event)) {
                    commandInterpreter.revertLast();
                    event.consume(); // <-- stops passing the event to next node
                }
            }
        });

        root.getStylesheets().add("stylesheet.css");
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("_File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));
        exitItem.setOnAction(ae -> Platform.exit());

        fileMenu.getItems().add(exitItem);
        menuBar.getMenus().add(fileMenu);

        root.setTop(menuBar);
        stage.setScene(scene);
        stage.setTitle("TactView - Video editor");
        stage.setMaximized(true);

        VBox vbox = new VBox(2); // spacing between child nodes only.
        vbox.setId("content-area");
        vbox.setMinHeight(300);
        vbox.setPrefWidth(scene.getWidth());
        vbox.setPadding(new Insets(1)); // space between vbox border and child nodes column

        GridPane upper = new GridPane();
        upper.setHgap(5);
        upper.setVgap(5);
        upper.setId("upper-content-area");
        ColumnConstraints column1 = new ColumnConstraints(300, 600, 1000);
        column1.setHgrow(Priority.ALWAYS);
        ColumnConstraints column2 = new ColumnConstraints(300, 300, 500);
        upper.getColumnConstraints().addAll(column1, column2);

        FlowPane leftHBox = new FlowPane(Orientation.HORIZONTAL, 5, 5);
        leftHBox.setPrefWidth(scene.getWidth() - 300);
        leftHBox.setId("effect-view");

        leftHBox.getChildren().add(createIcon("file:/home/black/.config/google-chrome/Default_old/Extensions/hbdkkfheckcdppiaiabobmennhijkknn/9.6.0.0_0/image/logo-32.png"));
        leftHBox.getChildren().add(createIcon("file:/home/black/.config/google-chrome/Default_old/Extensions/hbdkkfheckcdppiaiabobmennhijkknn/9.6.0.0_0/image/logo-32.png"));
        leftHBox.getChildren().add(createIcon("file:/home/black/.config/google-chrome/Default_old/Extensions/hbdkkfheckcdppiaiabobmennhijkknn/9.6.0.0_0/image/logo-32.png"));
        leftHBox.getChildren().add(createIcon("file:/home/black/.config/google-chrome/Default_old/Extensions/hbdkkfheckcdppiaiabobmennhijkknn/9.6.0.0_0/image/logo-32.png"));
        leftHBox.getChildren().add(createIcon("file:/home/black/.config/google-chrome/Default_old/Extensions/hbdkkfheckcdppiaiabobmennhijkknn/9.6.0.0_0/image/logo-32.png"));
        leftHBox.getChildren().add(createIcon("file:/home/black/.config/google-chrome/Default_old/Extensions/hbdkkfheckcdppiaiabobmennhijkknn/9.6.0.0_0/image/logo-32.png"));

        VBox rightVBox = new VBox(5);
        rightVBox.setPrefWidth(300);
        rightVBox.setId("clip-view");

        canvas = new Canvas(W, H);
        canvas.getGraphicsContext2D().setFill(new Color(0.0, 0.0, 0.0, 1.0));
        canvas.getGraphicsContext2D().fillRect(0, 0, W, H);
        rightVBox.getChildren().add(canvas);

        videoTimestampLabel = new Label("00:00:00.000");
        videoTimestampLabel.setId("video-timestamp-label");
        HBox videoStatusBar = new HBox(10);
        videoStatusBar.setId("video-status-bar");
        videoStatusBar.getChildren().add(videoTimestampLabel);
        rightVBox.getChildren().add(videoStatusBar);

        HBox underVideoBar = new HBox(1);
        Button playButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.PLAY));
        playButton.setOnMouseClicked(e -> uiTimelineManager.startPlayback());
        Button stopButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.STOP));
        stopButton.setOnMouseClicked(e -> uiTimelineManager.stopPlayback());
        Button jumpBackButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.FAST_BACKWARD));
        jumpBackButton.setOnMouseClicked(e -> uiTimelineManager.jumpRelative(BigDecimal.valueOf(-10)));
        Button jumpForwardButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.FAST_FORWARD));
        jumpForwardButton.setOnMouseClicked(e -> uiTimelineManager.jumpRelative(BigDecimal.valueOf(10)));

        underVideoBar.getChildren().add(jumpBackButton);
        underVideoBar.getChildren().add(playButton);
        underVideoBar.getChildren().add(stopButton);
        underVideoBar.getChildren().add(jumpForwardButton);
        underVideoBar.setId("video-button-bar");
        rightVBox.getChildren().add(underVideoBar);

        upper.add(leftHBox, 0, 0);
        upper.add(rightVBox, 1, 0);

        VBox lower = new VBox(5);
        lower.setPrefWidth(scene.getWidth());
        lower.setId("timeline-view");

        lower.getChildren().add(uiTimeline.createTimeline());

        vbox.getChildren().addAll(upper, lower);

        root.setCenter(vbox);
        pane.setContent(root);

        stage.show();
    }

    private VBox createIcon(String file) {
        ImageView image = new ImageView(file);
        image.setPreserveRatio(true);
        image.setFitWidth(50);
        Label text = new Label();
        text.setText("effect1");

        VBox vbox = new VBox();
        vbox.getStyleClass().add("icon");
        vbox.getChildren().addAll(image, text);
        vbox.setPadding(new Insets(10));

        vbox.setOnDragDetected(event -> {
            /* drag was detected, start drag-and-drop gesture */
            System.out.println("onDragDetected");

            /* allow any transfer mode */
            Dragboard db = vbox.startDragAndDrop(TransferMode.ANY);

            /* put a string on dragboard */
            ClipboardContent content = new ClipboardContent();
            content.putString("effect1");
            db.setContent(content);

            event.consume();
        });

        return vbox;
    }

    public static void main(String[] args) {
        LightDiContextConfiguration configuration = LightDiContextConfiguration.builder()
                .withThreadNumber(4)
                .withCheckForIntegrity(true)
                .withAdditionalDependencies(Collections.singletonList(new JnaLightDiPlugin()))
                .build();
        lightDi = LightDi.initContextByClass(MainApplicationConfiguration.class, configuration);
        playbackController = lightDi.getBean(PlaybackController.class);
        uiTimeline = lightDi.getBean(UiTimeline.class);
        uiTimelineManager = lightDi.getBean(UiTimelineManager.class);
        uiTimelineManager.registerUiConsumer(position -> uiTimeline.updateLine(position));
        uiTimelineManager.registerUiConsumer(position -> updateTime(position));
        uiTimelineManager.registerConsumer(position -> updateDisplay(position));
        commandInterpreter = lightDi.getBean(UiCommandInterpreterService.class);
        lightDi.eagerInitAllBeans();

        launch(args);
    }

    private static void updateTime(TimelinePosition position) {
        long wholePartOfTime = position.getSeconds().longValue();
        long hours = wholePartOfTime / 3600;
        long minutes = (wholePartOfTime - hours * 3600) / 60;
        long seconds = (wholePartOfTime - hours * 3600 - minutes * 60);
        long millis = position.getSeconds().remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(1000)).longValue();

        String newLabel = String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);

        videoTimestampLabel.setText(newLabel);
    }

    private static void updateDisplay(TimelinePosition currentPosition) {
        Image actualImage = playbackController.getFrameAt(currentPosition);

        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.drawImage(actualImage, 0, 0, W, H);
        });
    }

    public void launchUi() {
        launch();
    }

}