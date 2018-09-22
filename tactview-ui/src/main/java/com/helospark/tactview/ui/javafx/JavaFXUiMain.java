package com.helospark.tactview.ui.javafx;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.controlsfx.control.NotificationPane;

import com.helospark.lightdi.LightDi;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.LightDiContextConfiguration;
import com.helospark.tactview.core.Main;
import com.helospark.tactview.core.decoder.MediaDataRequest;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.decoder.ffmpeg.FFmpegBasedMediaDecoderDecorator;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.jpaplugin.JnaLightDiPlugin;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
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
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class JavaFXUiMain extends Application {
    public static final int W = 320; // canvas dimensions.
    public static final int H = 260;

    static LightDiContext lightDi;

    public static final double D = 20; // diameter.

    private static UiCommandInterpreterService commandInterpreter;
    private static FFmpegBasedMediaDecoderDecorator mediaDecoder;
    private static MediaMetadata metadata;

    static BufferedImage bufferedImage;

    int frames = 0;
    double zoom = 1.0;
    long currentTime = System.currentTimeMillis();
    static int second = 0;

    Rectangle draggedItem = null;
    int dragIndex = -1;
    Pane draggedChannel = null;

    static List<BufferedImage> images = new ArrayList<>(30);
    static List<BufferedImage> backBuffer = new ArrayList<>(30);
    volatile static boolean backBufferReady = false;

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

        HBox rightHBox = new HBox(5);
        rightHBox.setPrefWidth(300);
        rightHBox.setId("clip-view");

        final Canvas canvas = new Canvas(W, H);
        rightHBox.getChildren().add(canvas);

        upper.add(leftHBox, 0, 0);
        upper.add(rightHBox, 1, 0);

        VBox lower = new VBox(5);
        lower.setPrefWidth(scene.getWidth());
        lower.setId("timeline-view");

        ScrollPane timeLineScrollPane = new ScrollPane();
        VBox group = new VBox();
        group.setPrefWidth(2000);

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

            //            Rectangle rec = new Rectangle(100, 50);
            //            rec.setTranslateX(600);
            //            timelineClipsGroup.getChildren().add(rec);

            final int index = i;

            timeline.setOnDragEntered(event -> {
                draggedItem = new Rectangle(300, 50);
                dragIndex = index;
                draggedChannel = timelineClipsGroup;
                timelineClipsGroup.getChildren().add(draggedItem);
            });

            timeline.setOnDragExited(event -> {
                if (draggedItem != null) {
                    timelineClipsGroup.getChildren().remove(draggedItem);
                    draggedItem = null;
                }
            });

            timeline.setOnDragOver(event -> {
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
                System.out.println("CHANNEL " + index + " dragdone " + event.getX() + " " + event.getY() + " " + timeLineScrollPane.getVvalue());
                //                commandInterpreter.sendAndGetCommand(new VideoClipAddedCommand("file:doesntmatter", 1234), ClipAddedResponse.class)
                //                        .thenAccept(response -> {
                //                            Platform.runLater(() -> {
                //                                Notifications.create()
                //                                        .title("Some cool info")
                //                                        .text("It was updated as you requested")
                //                                        .position(Pos.BOTTOM_RIGHT)
                //                                        .hideAfter(Duration.seconds(2))
                //                                        .darkStyle()
                //                                        .showInformation();
                //                            });
                //                        });
                draggedItem = null;
            });

            group.getChildren().add(timeline);
        }

        /// ZZZOOOMM

        group.setOnScroll(evt -> {
            if (evt.isControlDown()) {
                evt.consume();
                group.setScaleX(zoom);
                zoom += evt.getDeltaY() > 0 ? 0.1 : -0.1;
            }
        });

        /// ZOOOM

        timeLineScrollPane.setContent(group);
        lower.getChildren().add(timeLineScrollPane);

        vbox.getChildren().addAll(upper, lower);

        root.setCenter(vbox);
        pane.setContent(root);

        //        vbox.getChildren().add(pane);

        updateImage(canvas);
        dump(root);
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

    private void updateImage(Canvas canvas) {
        DoubleProperty x = new SimpleDoubleProperty();
        DoubleProperty y = new SimpleDoubleProperty();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(x, 0),
                        new KeyValue(y, 0)),
                new KeyFrame(Duration.seconds(3),
                        new KeyValue(x, W - D),
                        new KeyValue(y, H - D)));
        timeline.setAutoReverse(true);
        timeline.setCycleCount(Timeline.INDEFINITE);

        try {
            BufferedImage asd = ImageIO.read(new File("/home/black/Pictures/b.PNG"));
            BufferedImage bsd = ImageIO.read(new File("/home/black/Pictures/a.png"));

            AnimationTimer timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    if (frames == images.size()) {
                        if (!backBufferReady) {
                            return;
                        }
                        List<BufferedImage> tmp = images;
                        images = backBuffer;
                        backBuffer = tmp;
                        fillImageBuffers();
                        frames = 0;
                    }
                    if (images.size() == 0) {
                        return;
                    }
                    BufferedImage imageToDraw = images.get(frames);

                    WritableImage actualImage = SwingFXUtils.toFXImage(imageToDraw, null);

                    gc.drawImage(actualImage, 0, 0, W, H);

                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ++frames;
                    //                    if (System.currentTimeMillis() - currentTime > 1000) {
                    //                        System.out.println((double) frames / ((System.currentTimeMillis() - currentTime) / 1000.0));
                    //                        currentTime = System.currentTimeMillis();
                    //                    }
                }
            };

            timer.start();
            timeline.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LightDiContextConfiguration configuration = LightDiContextConfiguration.builder()
                .withThreadNumber(4)
                .withCheckForIntegrity(true)
                .withAdditionalDependencies(Collections.singletonList(new JnaLightDiPlugin()))
                .build();
        lightDi = LightDi.initContextByClass(Main.class, configuration);
        //        commandInterpreter = lightDi.getBean(UiCommandInterpreterService.class);
        mediaDecoder = lightDi.getBean(FFmpegBasedMediaDecoderDecorator.class);
        metadata = mediaDecoder.readMetadata(new File("/home/black/Documents/pic_tetris.mp4"));
        System.out.println(metadata);

        fillImageBuffers();

        launch(args);
    }

    private static void fillImageBuffers() {
        System.out.println("loading: " + second);
        new Thread(() -> {
            backBufferReady = false;
            MediaDataResponse response = mediaDecoder.readFrames(MediaDataRequest.builder()
                    .withFile(new File("/home/black/Documents/pic_tetris.mp4"))
                    .withWidth(320)
                    .withHeight(260)
                    .withStart(new TimelinePosition(BigDecimal.valueOf(second)))
                    .withLength(new TimelineLength(new BigDecimal(5)))
                    .withMetadata(metadata)
                    .build());
            setImage(response.getVideoFrames(), backBuffer);
            second += 5;
            backBufferReady = true;
        }).start();
    }

    public Image getJavaFXImage(BufferedImage bufferedImage, int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", out);
            out.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return new javafx.scene.image.Image(in);
    }

    public static void dump(Node n) {
        dump(n, 0);
    }

    private static void dump(Node n, int depth) {
        for (int i = 0; i < depth; i++)
            System.out.print("  ");
        System.out.println(n);
        if (n instanceof Parent)
            for (Node c : ((Parent) n).getChildrenUnmodifiable())
                dump(c, depth + 1);
    }

    public JavaFXUiMain() {
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

    public static void setImage(List<ByteBuffer> byteBuffers, List<BufferedImage> currentList) {
        currentList.clear();
        long start = System.currentTimeMillis();
        for (ByteBuffer byteBuffer : byteBuffers) {
            bufferedImage = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < H; ++i) {
                for (int j = 0; j < W; ++j) {
                    int in = i * W * 4 + (j * 4);
                    int r = printByte(byteBuffer.get(in + 0));
                    int g = printByte(byteBuffer.get(in + 1));
                    int b = printByte(byteBuffer.get(in + 2));
                    bufferedImage.setRGB(j, i, new java.awt.Color(r, g, b).getRGB());
                }
            }
            currentList.add(bufferedImage);
        }
        System.out.println("Processing images took " + (System.currentTimeMillis() - start));
    }

    public static int printByte(byte b) {
        int value;
        if (b < 0) {
            value = 256 + b;
        } else {
            value = b;
        }
        return value;
    }
}