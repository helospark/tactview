package com.helospark.tactview.ui.javafx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.LightDi;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.LightDiContextConfiguration;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.decoder.ffmpeg.FFmpegBasedMediaDecoderDecorator;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.jpaplugin.JnaLightDiPlugin;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class JavaFXUiMain extends Application {
    public static final int W = 320; // canvas dimensions.
    public static final int H = 260;

    static LightDiContext lightDi;

    private static final BigDecimal PIXEL_PER_SECOND = new BigDecimal(10L);

    private static UiCommandInterpreterService commandInterpreter;
    private static FFmpegBasedMediaDecoderDecorator mediaDecoder;
    private static PlaybackController playbackController;
    private static TimelineManager timelineManager; // todo: not directly here, have to be via commands

    static BufferedImage bufferedImage;

    int frames = 0;
    double zoom = 1.0;
    long currentTime = System.currentTimeMillis();
    static int second = 0;

    Rectangle draggedItem = null;
    int dragIndex = -1;
    Pane draggedChannel = null;
    private static TimelineImagePatternService timelineImagePatternService;

    static List<BufferedImage> images = new ArrayList<>(30);
    static List<BufferedImage> backBuffer = new ArrayList<>(30);
    volatile static boolean backBufferReady = false;

    static UiTimelineManager uiTimelineManager;
    private static Line line;
    private static Canvas canvas;
    private static Label videoTimestampLabel;

    @Override
    public void start(Stage stage) throws IOException {
        //        updateImage(stage);
        NotificationPane pane = new NotificationPane();
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 650, 550, Color.GREY);
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

        ScrollPane timeLineScrollPane = new ScrollPane();
        Group timelineGroup = new Group();
        VBox timelineBoxes = new VBox();
        timelineBoxes.setPrefWidth(2000);
        timelineGroup.getChildren().add(timelineBoxes);

        line = new Line();
        line.setStartY(0);
        line.setEndY(300);
        line.setStartX(0);
        line.setEndX(0);
        line.setId("timeline-position-line");
        timelineGroup.getChildren().add(line);

        for (int i = 0; i < 10; ++i) {
            HBox timeline = new HBox();
            timeline.setPrefHeight(50);
            timeline.setMinHeight(50);
            timeline.getStyleClass().add("timelinerow");
            timeline.setPrefWidth(2000);
            timeline.setMinWidth(2000);

            VBox timelineTitle = new VBox();
            timelineTitle.getChildren().add(new Label("Video line 1"));
            timelineTitle.setMaxWidth(200);
            timelineTitle.setPrefHeight(50);
            timelineTitle.getStyleClass().add("timeline-title");
            timeline.getChildren().add(timelineTitle);

            Pane timelineClipsGroup = new Pane();
            timelineClipsGroup.minWidth(2000);
            timelineClipsGroup.minHeight(50);
            timelineClipsGroup.getStyleClass().add("timeline-clips");
            timeline.getChildren().add(timelineClipsGroup);

            final int index = i;

            timeline.setOnDragEntered(event -> {
                Dragboard db = event.getDragboard();
                System.out.println("a " + db.getFiles().size());
                if (!db.getFiles().isEmpty()) {
                    draggedItem = new Rectangle(300, 50);
                    dragIndex = index;
                    draggedChannel = timelineClipsGroup;
                    timelineClipsGroup.getChildren().add(draggedItem);
                    File file = db.getFiles().get(0);
                    System.out.println(file);
                    CompletableFuture.supplyAsync(() -> {
                        return mediaDecoder.readMetadata(file);
                    }).exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    }).thenAccept(b -> {
                        int width = b.getLength().getSeconds().multiply(PIXEL_PER_SECOND).intValue();
                        System.out.println("Setting width to " + width);
                        if (draggedItem != null) {
                            draggedItem.setWidth(width);
                        }
                    });
                }
            });

            timeline.setOnDragExited(event -> {
                if (draggedItem != null) {
                    timelineClipsGroup.getChildren().remove(draggedItem);
                    draggedItem = null;
                }
            });

            timeline.setOnDragOver(event -> {
                System.out.println("c " + event.getDragboard().getFiles().size());
                System.out.println("CHANNEL " + index + " onDragOver " + event.getX() + " " + event.getY() + " " + timeLineScrollPane.getVvalue());

                draggedItem.setTranslateX(event.getX() - timelineClipsGroup.getLayoutX());

                if (event.getY() > 180) {
                    timeLineScrollPane.setVvalue(timeLineScrollPane.getVvalue() + 0.01);
                }
                if (event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }

                event.consume();
            });
            timeline.setOnDragDropped(event -> {
                Rectangle droppedElement = draggedItem;
                draggedItem = null;
                System.out.println("CHANNEL " + index + " dragdone " + event.getX() + " " + event.getY() + " " + timeLineScrollPane.getVvalue());
                if (event.getDragboard().hasFiles()) {
                    File file = event.getDragboard().getFiles().get(0);
                    MediaMetadata metadata = mediaDecoder.readMetadata(file);
                    int timelineWidth = (int) (droppedElement.getWidth() * timelineBoxes.getScaleX());
                    // map x to position
                    BigDecimal position = new BigDecimal(event.getX())
                            .multiply(BigDecimal.ONE) // zoom dummy
                            .subtract(BigDecimal.ZERO) // scroll dummy
                            .divide(PIXEL_PER_SECOND);

                    // todo: command pattern here
                    timelineManager.onResourceAdded(index, new TimelinePosition(position), file.getAbsolutePath());

                    timelineImagePatternService.createTimelinePattern(file, metadata, timelineWidth)
                            .exceptionally(e -> {
                                e.printStackTrace();
                                return null;
                            })
                            .thenAccept(fillImage -> {
                                System.out.println("Setting image");
                                if (droppedElement != null) {
                                    Platform.runLater(() -> droppedElement.setFill(new ImagePattern(fillImage)));
                                }
                            });

                }
            });
            timelineBoxes.getChildren().add(timeline);
        }
        /// ZZZOOOMM

        timelineBoxes.setOnScroll(evt -> {
            if (evt.isControlDown()) {
                evt.consume();
                double factor = 1.0;
                factor += evt.getDeltaY() > 0 ? 0.1 : -0.1;

                VBox node = timelineBoxes;
                double x = evt.getX();

                double oldScale = node.getScaleX();
                double scale = oldScale * factor;
                if (scale < 0.05)
                    scale = 0.05;
                if (scale > 50)
                    scale = 50;
                node.setScaleX(scale);

                double f = (scale / oldScale) - 1;
                Bounds bounds = node.localToScene(node.getBoundsInLocal());
                double dx = (x - (bounds.getWidth() / 2 + bounds.getMinX()));

                node.setTranslateX(node.getTranslateX() - f * dx);
            }
        });
        /// ZOOOM

        timeLineScrollPane.setContent(timelineGroup);
        lower.getChildren().add(timeLineScrollPane);

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
        mediaDecoder = lightDi.getBean(FFmpegBasedMediaDecoderDecorator.class);
        timelineImagePatternService = lightDi.getBean(TimelineImagePatternService.class);
        playbackController = lightDi.getBean(PlaybackController.class);
        timelineManager = lightDi.getBean(TimelineManager.class);
        uiTimelineManager = lightDi.getBean(UiTimelineManager.class);
        uiTimelineManager.registerUiConsumer(position -> updateLine(position));
        uiTimelineManager.registerUiConsumer(position -> updateTime(position));
        uiTimelineManager.registerConsumer(position -> updateDisplay(position));

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

    private static void updateLine(TimelinePosition position) {
        int pixel = position.getSeconds().multiply(PIXEL_PER_SECOND).intValue();
        line.setStartX(pixel);
        line.setEndX(pixel);
    }

    /**
     * Required param, however JavaFx required empty constructor and create the instance by itself.
     * Why do you do this to me JavaFx???
     */
    public static void setCommandInterpreter(UiCommandInterpreterService commandInterpreter) {
        JavaFXUiMain.commandInterpreter = commandInterpreter;
    }

    public void launchUi() {
        launch();
    }

}