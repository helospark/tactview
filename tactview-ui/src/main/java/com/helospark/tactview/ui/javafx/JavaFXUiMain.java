package com.helospark.tactview.ui.javafx;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.LightDi;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.LightDiContextConfiguration;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.jpaplugin.JnaLightDiPlugin;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.menu.MenuProcessor;
import com.helospark.tactview.ui.javafx.render.RenderDialogOpener;
import com.helospark.tactview.ui.javafx.render.SingleFullImageViewController;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.save.DirtyRepository;
import com.helospark.tactview.ui.javafx.save.ExitWithSaveService;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;
import com.helospark.tactview.ui.javafx.tabs.TabFactory;
import com.helospark.tactview.ui.javafx.uicomponents.PropertyView;
import com.helospark.tactview.ui.javafx.uicomponents.UiTimeline;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JavaFXUiMain extends Application {
    public static Stage STAGE = null;
    public static final int W = 320; // canvas dimensions.
    public static final int H = 260;

    static LightDiContext lightDi;

    static BufferedImage bufferedImage;

    int frames = 0;
    long currentTime = System.currentTimeMillis();
    static int second = 0;

    static List<BufferedImage> images = new ArrayList<>(30);
    static List<BufferedImage> backBuffer = new ArrayList<>(30);
    volatile static boolean backBufferReady = false;

    static UiTimelineManager uiTimelineManager;
    private static Canvas canvas;
    private static Label videoTimestampLabel;
    static UiTimeline uiTimeline;
    static UiProjectRepository uiProjectRepository;
    static PropertyView effectPropertyView;
    static RenderDialogOpener renderService;
    static DisplayUpdaterService displayUpdateService;

    @Override
    public void start(Stage stage) throws IOException {
        DirtyRepository dirtyRepository = lightDi.getBean(DirtyRepository.class);
        ExitWithSaveService exitWithSaveService = lightDi.getBean(ExitWithSaveService.class);

        JavaFXUiMain.STAGE = stage;
        NotificationPane notificationPane = new NotificationPane();
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 650, 550, Color.GREY);

        root.getStylesheets().add("stylesheet.css");

        MenuBar menuBar = lightDi.getBean(MenuProcessor.class).createMenuBar();

        stage.setOnCloseRequest(e -> {
            exitApplication(exitWithSaveService);
        });

        root.setTop(menuBar);
        stage.setScene(scene);
        stage.setTitle("TactView - Video editor");
        dirtyRepository.addUiChangeListener(value -> {
            String title = "";
            if (value) {
                title += "* ";
            }
            title += "TactView - Video editor";
            stage.setTitle(title);
        });
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
        ColumnConstraints column1 = new ColumnConstraints(300, 300, 1000);
        column1.setHgrow(Priority.ALWAYS);
        ColumnConstraints column2 = new ColumnConstraints(300, 600, 500);
        upper.getColumnConstraints().addAll(column1, column2);
        upper.setMaxHeight(400);

        TabPane tabPane = new TabPane();
        lightDi.getListOfBeans(TabFactory.class)
                .stream()
                .forEach(tabFactory -> {
                    Tab tab = tabFactory.createTabContent();
                    tabPane.getTabs().add(tab);
                });

        VBox rightVBox = new VBox(5);
        rightVBox.setPrefWidth(300);
        rightVBox.setId("clip-view");

        canvas = new Canvas();
        canvas.widthProperty().bind(uiProjectRepository.getPreviewWidthProperty());
        canvas.heightProperty().bind(uiProjectRepository.getPreviewHeightProperty());
        canvas.getGraphicsContext2D().setFill(new Color(0.0, 0.0, 0.0, 1.0));
        canvas.getGraphicsContext2D().fillRect(0, 0, W, H);
        InputModeRepository inputModeRepository = lightDi.getBean(InputModeRepository.class);
        inputModeRepository.setCanvas(canvas);
        displayUpdateService.setCanvas(canvas);
        rightVBox.getChildren().add(canvas);

        videoTimestampLabel = new Label("00:00:00.000");
        videoTimestampLabel.setId("video-timestamp-label");
        HBox videoStatusBar = new HBox(10);
        videoStatusBar.setId("video-status-bar");
        videoStatusBar.getChildren().add(videoTimestampLabel);
        rightVBox.getChildren().add(videoStatusBar);

        HBox underVideoBar = new HBox(1);
        ToggleButton muteButton = new ToggleButton("", new Glyph("FontAwesome", FontAwesome.Glyph.VOLUME_OFF));
        muteButton.setSelected(false);
        muteButton.setOnAction(event -> {
            lightDi.getBean(UiPlaybackPreferenceRepository.class).setMute(muteButton.isSelected());
        });

        SingleFullImageViewController fullScreenRenderer = lightDi.getBean(SingleFullImageViewController.class);
        Button fullscreenButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.IMAGE));
        fullscreenButton.setOnMouseClicked(e -> fullScreenRenderer.renderFullScreenAtCurrentLocation());
        Button playButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.PLAY));
        playButton.setOnMouseClicked(e -> uiTimelineManager.startPlayback());
        Button stopButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.STOP));
        stopButton.setOnMouseClicked(e -> uiTimelineManager.stopPlayback());
        Button jumpBackOnFrameButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.STEP_BACKWARD));
        jumpBackOnFrameButton.setOnMouseClicked(e -> uiTimelineManager.moveBackOneFrame());
        Button jumpForwardOnFrameButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.STEP_FORWARD));
        jumpForwardOnFrameButton.setOnMouseClicked(e -> uiTimelineManager.moveForwardOneFrame());
        Button jumpBackButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.BACKWARD));
        jumpBackButton.setOnMouseClicked(e -> uiTimelineManager.jumpRelative(BigDecimal.valueOf(-10)));
        Button jumpForwardButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.FORWARD));
        jumpForwardButton.setOnMouseClicked(e -> uiTimelineManager.jumpRelative(BigDecimal.valueOf(10)));

        underVideoBar.getChildren().add(muteButton);
        underVideoBar.getChildren().add(fullscreenButton);
        underVideoBar.getChildren().add(jumpBackButton);
        underVideoBar.getChildren().add(jumpBackOnFrameButton);
        underVideoBar.getChildren().add(playButton);
        underVideoBar.getChildren().add(stopButton);
        underVideoBar.getChildren().add(jumpForwardOnFrameButton);
        underVideoBar.getChildren().add(jumpForwardButton);
        underVideoBar.setId("video-button-bar");
        rightVBox.getChildren().add(underVideoBar);

        FlowPane propertyBox = effectPropertyView.getPropertyWindow();
        ScrollPane propertyBoxScrollPane = new ScrollPane(propertyBox);
        propertyBoxScrollPane.setFitToWidth(true);
        upper.add(propertyBoxScrollPane, 0, 0);
        upper.add(tabPane, 1, 0);
        upper.add(rightVBox, 2, 0);

        VBox lower = new VBox(5);
        lower.setPrefWidth(scene.getWidth());
        lower.setPrefHeight(300);
        lower.setId("timeline-view");

        Node timeline = uiTimeline.createTimeline(lower, root);
        lower.getChildren().add(timeline);
        VBox.setVgrow(timeline, Priority.ALWAYS);

        vbox.getChildren().addAll(upper, lower);

        root.setCenter(vbox);
        notificationPane.setContent(root);

        inputModeRepository.registerInputModeChangeConsumerr(onClassChange(lower));
        inputModeRepository.registerInputModeChangeConsumerr(onClassChange(tabPane));
        inputModeRepository.registerInputModeChangeConsumerr(onClassChange(propertyBox));

        lightDi.getListOfBeans(ScenePostProcessor.class)
                .stream()
                .forEach(processor -> processor.postProcess(scene));

        lightDi.getBean(UiInitializer.class).initialize();

        stage.show();
    }

    private void exitApplication(ExitWithSaveService exitWithSaveService) {
        exitWithSaveService.optionallySaveAndThenRun(() -> {
            Platform.exit();
            System.exit(0);
        });
    }

    private static Consumer<Boolean> onClassChange(Node element) {
        return enabled -> {
            if (enabled) {
                element.getStyleClass().add("input-mode-enabled");
            } else {
                element.getStyleClass().remove("input-mode-enabled");
            }
        };
    }

    @Override
    public void init() throws Exception {
        super.init();
        LightDiContextConfiguration configuration = LightDiContextConfiguration.builder()
                .withThreadNumber(4)
                .withCheckForIntegrity(true)
                .withAdditionalDependencies(Collections.singletonList(new JnaLightDiPlugin()))
                .withUseClasspathFile(false)
                .build();
        lightDi = LightDi.initContextByClass(MainApplicationConfiguration.class, configuration);
        uiTimeline = lightDi.getBean(UiTimeline.class);
        uiTimelineManager = lightDi.getBean(UiTimelineManager.class);
        effectPropertyView = lightDi.getBean(PropertyView.class);
        uiTimelineManager.registerUiPlaybackConsumer(position -> uiTimeline.updateLine(position));
        uiTimelineManager.registerUiPlaybackConsumer(position -> effectPropertyView.updateValues(position));
        uiTimelineManager.registerUiPlaybackConsumer(position -> updateTime(position));
        displayUpdateService = lightDi.getBean(DisplayUpdaterService.class);
        uiTimelineManager.registerPlaybackConsumer(position -> displayUpdateService.updateDisplay(position));
        AudioUpdaterService audioUpdaterService = lightDi.getBean(AudioUpdaterService.class);
        uiTimelineManager.registerPlaybackConsumer(position -> audioUpdaterService.updateAtPosition(position));
        uiTimelineManager.registerStoppedConsumer(type -> {
            if (type.equals(UiTimelineManager.PlaybackStatus.STOPPED)) {
                audioUpdaterService.playbackStopped();
            }
        });

        uiProjectRepository = lightDi.getBean(UiProjectRepository.class);
        renderService = lightDi.getBean(RenderDialogOpener.class);
        lightDi.eagerInitAllBeans();
    }

    public static void main(String[] args) {
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

    public void launchUi() {
        launch();
    }

}