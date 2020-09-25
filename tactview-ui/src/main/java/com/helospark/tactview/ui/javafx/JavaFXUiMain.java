package com.helospark.tactview.ui.javafx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.LightDiContextConfiguration;
import com.helospark.lightdi.properties.Environment;
import com.helospark.lightdi.properties.PropertySourceHolder;
import com.helospark.tactview.core.plugin.PluginMainClassProviders;
import com.helospark.tactview.core.save.DirtyRepository;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.jpaplugin.JnaLightDiPlugin;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.menu.MenuProcessor;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.projectsize.ProjectSizeInitializer;
import com.helospark.tactview.ui.javafx.render.RenderDialogOpener;
import com.helospark.tactview.ui.javafx.render.SingleFullImageViewController;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.save.ExitWithSaveService;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;
import com.helospark.tactview.ui.javafx.tabs.TabActiveRequest;
import com.helospark.tactview.ui.javafx.tabs.TabFactory;
import com.helospark.tactview.ui.javafx.uicomponents.PropertyView;
import com.helospark.tactview.ui.javafx.uicomponents.ScaleComboBoxFactory;
import com.helospark.tactview.ui.javafx.uicomponents.UiTimeline;
import com.helospark.tactview.ui.javafx.uicomponents.VideoStatusBarUpdater;
import com.helospark.tactview.ui.javafx.uicomponents.audiocomponent.AudioVisualizationComponent;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class JavaFXUiMain extends Application {
    public static Stage STAGE = null;

    static LightDiContext lightDi;

    static BufferedImage bufferedImage;

    private Stage splashStage;
    private ImageView splasViewh;

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
    static ProjectSizeInitializer projectSizeInitializer;

    @Override
    public void start(Stage stage) throws IOException {
        DirtyRepository dirtyRepository = lightDi.getBean(DirtyRepository.class);
        ExitWithSaveService exitWithSaveService = lightDi.getBean(ExitWithSaveService.class);

        JavaFXUiMain.STAGE = stage;
        NotificationPane notificationPane = new NotificationPane();
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 650, 550, Color.GREY);

        StylesheetAdderService styleSheetAdder = lightDi.getBean(StylesheetAdderService.class);

        styleSheetAdder.addStyleSheets(root, "stylesheet.css");

        MenuBar menuBar = lightDi.getBean(MenuProcessor.class).createMenuBar();

        stage.setOnCloseRequest(e -> {
            exitApplication(exitWithSaveService);
        });

        root.setTop(menuBar);
        stage.setScene(scene);
        stage.setTitle("TactView - Video editor");
        dirtyRepository.addUiChangeListener(value -> {
            Platform.runLater(() -> {
                String title = "";
                if (value) {
                    title += "* ";
                }
                title += "TactView - Video editor";
                stage.setTitle(title);
            });
        });
        stage.setMaximized(true);

        SplitPane mainContentPane = new SplitPane(); // spacing between child nodes only.
        mainContentPane.setId("content-area");
        mainContentPane.setPrefWidth(scene.getWidth());
        mainContentPane.setPadding(new Insets(1)); // space between vbox border and child nodes column
        mainContentPane.setDividerPositions(0.6);

        System.out.println("$$$$$$$$$$$ Width=" + stage.getWidth() + " " + stage.getHeight());

        SplitPane upperPane = new SplitPane();
        upperPane.setId("upper-content-area");
        upperPane.setMinHeight(300);
        upperPane.setDividerPositions(0.2, 0.6, 0.2);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        lightDi.getListOfBeans(TabFactory.class).stream().forEach(tabFactory -> {
            Tab tab = tabFactory.createTabContent();
            tabPane.getTabs().add(tab);
        });
        lightDi.getBean(UiMessagingService.class).register(TabActiveRequest.class, message -> {
            tabPane.getTabs()
                    .stream()
                    .filter(tab -> Objects.equals(tab.getId(), message.getEditorId()))
                    .findFirst()
                    .ifPresent(foundTab -> tabPane.getSelectionModel().select(foundTab));
        });
        tabPane.getSelectionModel().selectedItemProperty()
                .addListener((e, oldValue, newValue) -> {
                    if (oldValue instanceof TabCloseListener) {
                        ((TabCloseListener) oldValue).tabClosed();
                    }
                });

        VBox rightVBox = new VBox(3);
        rightVBox.setAlignment(Pos.TOP_CENTER);
        rightVBox.setId("clip-view");

        canvas = new Canvas();
        canvas.widthProperty().bind(uiProjectRepository.getPreviewWidthProperty());
        canvas.heightProperty().bind(uiProjectRepository.getPreviewHeightProperty());
        canvas.getGraphicsContext2D().setFill(new Color(0.0, 0.0, 0.0, 1.0));
        canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        InputModeRepository inputModeRepository = lightDi.getBean(InputModeRepository.class);
        inputModeRepository.setCanvas(canvas);
        displayUpdateService.setCanvas(canvas);

        ScrollPane previewScrollPane = new ScrollPane(
                createCentered(canvas));
        previewScrollPane.setFitToWidth(true);
        previewScrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        previewScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        rightVBox.getChildren().add(previewScrollPane);
        AudioVisualizationComponent audioVisualazationComponent = lightDi
                .getBean(AudioVisualizationComponent.class);
        rightVBox.getChildren().add(audioVisualazationComponent.getCanvas());
        audioVisualazationComponent.clearCanvas();

        videoTimestampLabel = new Label("00:00:00.000");
        videoTimestampLabel.setId("video-timestamp-label");
        HBox videoStatusBar = new HBox(10);
        videoStatusBar.setId("video-status-bar");
        videoStatusBar.getChildren().add(videoTimestampLabel);
        rightVBox.getChildren().add(videoStatusBar);

        UiPlaybackPreferenceRepository playbackPreferenceRepository = lightDi.getBean(UiPlaybackPreferenceRepository.class);

        HBox underVideoBar = new HBox(1);
        ToggleButton muteButton = new ToggleButton("", new Glyph("FontAwesome",
                FontAwesome.Glyph.VOLUME_OFF));
        muteButton.setSelected(false);
        muteButton.setOnAction(event -> playbackPreferenceRepository.setMute(muteButton.isSelected()));
        muteButton.setTooltip(new Tooltip("Mute"));

        SingleFullImageViewController fullScreenRenderer = lightDi.getBean(SingleFullImageViewController.class);
        Button fullscreenButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.IMAGE));
        fullscreenButton.setOnMouseClicked(e -> fullScreenRenderer.renderFullScreenAtCurrentLocation());
        fullscreenButton.setTooltip(new Tooltip("Show full scale preview"));

        ToggleButton halfImageEffectButton = new ToggleButton("", new Glyph("FontAwesome", FontAwesome.Glyph.STAR_HALF_ALT));
        halfImageEffectButton.setSelected(false);
        halfImageEffectButton.setOnAction(e -> {
            playbackPreferenceRepository.setHalfEffect(halfImageEffectButton.isSelected());
            uiTimelineManager.refresh();
        });
        halfImageEffectButton.setTooltip(new Tooltip("Apply effects only on left side of preview"));

        Button playButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.PLAY));
        playButton.setOnMouseClicked(e -> uiTimelineManager.startPlayback());
        playButton.setTooltip(new Tooltip("Play"));

        Button stopButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.STOP));
        stopButton.setOnMouseClicked(e -> uiTimelineManager.stopPlayback());
        stopButton.setTooltip(new Tooltip("Stop"));

        Button jumpBackOnFrameButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.STEP_BACKWARD));
        jumpBackOnFrameButton.setOnMouseClicked(e -> uiTimelineManager.moveBackOneFrame());
        jumpBackOnFrameButton.setTooltip(new Tooltip("Step one frame back"));

        Button jumpForwardOnFrameButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.STEP_FORWARD));
        jumpForwardOnFrameButton.setOnMouseClicked(e -> uiTimelineManager.moveForwardOneFrame());
        jumpForwardOnFrameButton.setTooltip(new Tooltip("Step one frame forward"));

        Button jumpBackButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.BACKWARD));
        jumpBackButton.setOnMouseClicked(e -> uiTimelineManager.jumpRelative(BigDecimal.valueOf(-10)));
        jumpBackButton.setTooltip(new Tooltip("Step 10s back"));

        Button jumpForwardButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.FORWARD));
        jumpForwardButton.setOnMouseClicked(e -> uiTimelineManager.jumpRelative(BigDecimal.valueOf(10)));
        jumpForwardButton.setTooltip(new Tooltip("Step 10s forward"));

        ComboBox<String> sizeDropDown = lightDi.getBean(ScaleComboBoxFactory.class).create();

        underVideoBar.getChildren().add(sizeDropDown);
        underVideoBar.getChildren().add(muteButton);
        underVideoBar.getChildren().add(halfImageEffectButton);
        underVideoBar.getChildren().add(fullscreenButton);
        underVideoBar.getChildren().add(jumpBackButton);
        underVideoBar.getChildren().add(jumpBackOnFrameButton);
        underVideoBar.getChildren().add(playButton);
        underVideoBar.getChildren().add(stopButton);
        underVideoBar.getChildren().add(jumpForwardOnFrameButton);
        underVideoBar.getChildren().add(jumpForwardButton);
        underVideoBar.setId("video-button-bar");
        rightVBox.getChildren().add(underVideoBar);

        rightVBox.widthProperty().addListener((e, oldV, newV) -> {
            int availableWidth = (int) (rightVBox.getWidth() - 20); // TODO: calculate magic value
            uiProjectRepository.setPreviewAvailableWidth(availableWidth);
        });
        rightVBox.heightProperty().addListener((e, oldV, newV) -> {
            int availableHeight = (int) (rightVBox.getHeight() - 100); // TODO: calculate magic value
            uiProjectRepository.setPreviewAvailableHeight(availableHeight);
        });

        BorderPane rightBorderPane = new BorderPane();
        //        Label underVideoLabel = new Label();
        //        underVideoLabel.setWrapText(true);
        Tooltip tooltip = new Tooltip();

        StringProperty statusTextProperty = lightDi.getBean(VideoStatusBarUpdater.class).getTextProperty();
        tooltip.textProperty().bind(statusTextProperty);
        tooltip.setHideOnEscape(false);
        tooltip.setAutoHide(false);
        tooltip.setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);

        statusTextProperty.addListener((e, oldV, newV) -> {
            if (newV.length() > 0) {
                Bounds canvasBottom = canvas.localToScene(canvas.getBoundsInLocal());
                double x = canvasBottom.getMinX();
                double y = canvasBottom.getMaxY() + 60;
                tooltip.show(canvas, x, y);
            } else {
                tooltip.hide();
            }
        });
        tooltip.setWrapText(true);

        //        rightBorderPane.setBottom(underVideoLabel);

        rightBorderPane.setCenter(rightVBox);

        VBox propertyBox = effectPropertyView.getPropertyWindow();
        ScrollPane propertyBoxScrollPane = new ScrollPane(propertyBox);
        propertyBoxScrollPane.setFitToWidth(true);
        propertyBoxScrollPane.setPrefWidth(500);
        upperPane.getItems().add(propertyBoxScrollPane);
        upperPane.getItems().add(tabPane);
        upperPane.getItems().add(rightBorderPane);

        VBox lower = new VBox(5);
        lower.setPrefWidth(scene.getWidth());
        lower.setPrefHeight(300);
        lower.setId("timeline-view");

        BorderPane timeline = uiTimeline.createTimeline(lower, root);
        lower.getChildren().add(timeline);
        VBox.setVgrow(timeline, Priority.ALWAYS);

        mainContentPane.getItems().add(upperPane);
        mainContentPane.getItems().add(lower);
        mainContentPane.setOrientation(Orientation.VERTICAL);

        root.setCenter(mainContentPane);
        notificationPane.setContent(root);

        inputModeRepository.registerInputModeChangeConsumerr(onClassChange(lower));
        inputModeRepository.registerInputModeChangeConsumerr(onClassChange(tabPane));
        inputModeRepository.registerInputModeChangeConsumerr(onClassChange(propertyBox));

        lightDi.getListOfBeans(ScenePostProcessor.class)
                .stream()
                .forEach(processor -> processor.postProcess(scene));

        lightDi.getBean(ProjectInitializer.class).clearAndInitialize();

        if (splashStage.isShowing()) {
            stage.show();
            splashStage.toFront();
            FadeTransition fadeSplash = new FadeTransition(Duration.seconds(0.5), splasViewh);
            fadeSplash.setDelay(Duration.millis(800));
            fadeSplash.setFromValue(1.0);
            fadeSplash.setToValue(0.0);
            fadeSplash.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    splashStage.hide();
                }
            });
            fadeSplash.play();
        }
    }

    private void showSplash(Stage splashStage, ImageView splash) {
        StackPane splashLayout = new StackPane();
        splashLayout.setStyle("-fx-background-color: transparent;");
        splashLayout.getChildren().add(splash);
        Scene splashScene = new Scene(splashLayout, 690, 590);
        splashScene.setFill(Color.TRANSPARENT);
        splashStage.setScene(splashScene);
        splashStage.show();
    }

    private Node createCentered(Canvas canvas2) {
        GridPane outerPane = new GridPane();
        RowConstraints row = new RowConstraints();
        row.setPercentHeight(100);
        row.setFillHeight(false);
        row.setValignment(VPos.CENTER);
        outerPane.getRowConstraints().add(row);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(100);
        col.setFillWidth(false);
        col.setHalignment(HPos.CENTER);
        outerPane.getColumnConstraints().add(col);

        outerPane.add(canvas2, 0, 0);
        return outerPane;
    }

    private void exitApplication(ExitWithSaveService exitWithSaveService) {
        exitWithSaveService.optionallySaveAndThenRun(() -> {
            Platform.exit();
            lightDi.close();
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

        Platform.runLater(() -> {
            splashStage = new Stage(StageStyle.DECORATED);
            splashStage.setTitle("Tactview starting...");

            splasViewh = new ImageView(new Image(getClass().getResource("/tactview-splash.png").toString()));

            splashStage.initStyle(StageStyle.TRANSPARENT);
            showSplash(splashStage, splasViewh);
        });

        LightDiContextConfiguration configuration = LightDiContextConfiguration.builder()
                .withThreadNumber(4)
                .withCheckForIntegrity(true)
                .withAdditionalDependencies(Collections.singletonList(new JnaLightDiPlugin()))
                .withUseClasspathFile(false)
                .build();
        List<Class<?>> allClasses = new ArrayList<>();
        allClasses.add(MainApplicationConfiguration.class);
        allClasses.addAll(PluginMainClassProviders.getPluginClasses());
        lightDi = new LightDiContext(configuration);
        lightDi.addPropertySource(createInitialPropertySource());
        lightDi.loadDependencies(List.of(), allClasses);

        uiTimeline = lightDi.getBean(UiTimeline.class);
        uiTimelineManager = lightDi.getBean(UiTimelineManager.class);
        effectPropertyView = lightDi.getBean(PropertyView.class);
        AudioVisualizationComponent audioVisualizationComponent = lightDi.getBean(AudioVisualizationComponent.class);

        uiTimelineManager.registerUiPlaybackConsumer(position -> uiTimeline.updateLine(position));
        uiTimelineManager.registerUiPlaybackConsumer(position -> effectPropertyView.updateValues(position));
        uiTimelineManager.registerUiPlaybackConsumer(position -> updateTime(position));
        uiTimelineManager.registerUiPlaybackConsumer(position -> audioVisualizationComponent.updateAudioComponent(position));

        displayUpdateService = lightDi.getBean(DisplayUpdaterService.class);
        projectSizeInitializer = lightDi.getBean(ProjectSizeInitializer.class);
        uiTimelineManager.setDisplayUpdaterService(lightDi.getBean(DisplayUpdaterService.class));

        uiProjectRepository = lightDi.getBean(UiProjectRepository.class);
        renderService = lightDi.getBean(RenderDialogOpener.class);
        lightDi.eagerInitAllBeans();
    }

    private PropertySourceHolder createInitialPropertySource() {
        Map<String, String> propertyMap = new HashMap<>();
        try {
            propertyMap.put("tactview.installation.folder", new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent());
        } catch (Throwable t) {
            System.out.println("Cannot determine installation location");
            t.printStackTrace();
        }

        return new PropertySourceHolder(Environment.ENVIRONMENT_PROPERTY_ORDER + 1, propertyMap);
    }

    // Do NOT run this one. Run the one in {@link application.HackyMain}!
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