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
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralClipFactoryChainItem;
import com.helospark.tactview.core.util.jpaplugin.JnaLightDiPlugin;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;
import com.helospark.tactview.ui.javafx.uicomponents.PropertyView;
import com.helospark.tactview.ui.javafx.uicomponents.UiTimeline;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JavaFXUiMain extends Application {
    public static final int W = 320; // canvas dimensions.
    public static final int H = 260;

    static LightDiContext lightDi;

    static BufferedImage bufferedImage;

    int frames = 0;
    double zoom = 1.0;
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
    static UiRenderService renderService;
    static DisplayUpdaterService displayUpdateService;

    @Override
    public void start(Stage stage) throws IOException {
        NotificationPane pane = new NotificationPane();
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 650, 550, Color.GREY);

        root.getStylesheets().add("stylesheet.css");
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("_File");
        Menu project = new Menu("_Project");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN));
        exitItem.setOnAction(ae -> {
            Platform.exit();
            System.exit(0);
        });
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        MenuItem render = new MenuItem("Render");
        render.setOnAction(e -> {
            renderService.renderProject();
        });

        project.getItems().add(render);

        fileMenu.getItems().add(exitItem);
        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(project);

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

        TabPane tabPane = new TabPane();

        FlowPane effectTabContent = new FlowPane(Orientation.HORIZONTAL, 5, 5);
        // leftHBox.setPrefWidth(scene.getWidth() - 300);

        List<EffectFactory> effects = lightDi.getListOfBeans(EffectFactory.class);

        effects.stream()
                .forEach(factory -> {
                    effectTabContent.getChildren().add(createIcon("effect:" + factory.getEffectId(),
                            factory.getEffectName(),
                            "file:/home/black/.config/google-chrome/Default_old/Extensions/hbdkkfheckcdppiaiabobmennhijkknn/9.6.0.0_0/image/logo-32.png"));
                });
        Tab effectTab = new Tab();
        effectTab.setText("effects");

        ScrollPane effectScrollPane = new ScrollPane(effectTabContent);
        effectScrollPane.setFitToWidth(true);
        effectScrollPane.setId("effect-view");
        effectTab.setContent(effectScrollPane);
        tabPane.getTabs().add(effectTab);

        FlowPane proceduralClipTabContent = new FlowPane(Orientation.HORIZONTAL, 5, 5);
        List<ProceduralClipFactoryChainItem> proceduralClips = lightDi.getListOfBeans(ProceduralClipFactoryChainItem.class);
        proceduralClips.stream()
                .forEach(chainItem -> {
                    proceduralClipTabContent.getChildren().add(createIcon("clip:" + chainItem.getProceduralClipId(),
                            chainItem.getProceduralClipName(),
                            "file:/home/black/.config/google-chrome/Default_old/Extensions/hbdkkfheckcdppiaiabobmennhijkknn/9.6.0.0_0/image/icon-cache.png"));
                });
        Tab proceduralClipTab = new Tab();
        proceduralClipTab.setText("clips");
        proceduralClipTab.setContent(proceduralClipTabContent);
        tabPane.getTabs().add(proceduralClipTab);

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

        upper.add(effectPropertyView.getPropertyWindow(), 0, 0);
        upper.add(tabPane, 1, 0);
        upper.add(rightVBox, 2, 0);

        VBox lower = new VBox(5);
        lower.setPrefWidth(scene.getWidth());
        lower.setId("timeline-view");

        lower.getChildren().add(uiTimeline.createTimeline());

        vbox.getChildren().addAll(upper, lower);

        root.setCenter(vbox);
        pane.setContent(root);

        inputModeRepository.registerInputModeChangeConsumerr(onClassChange(lower));
        inputModeRepository.registerInputModeChangeConsumerr(onClassChange(tabPane));
        inputModeRepository.registerInputModeChangeConsumerr(onClassChange(effectPropertyView.getPropertyWindow()));

        lightDi.getListOfBeans(ScenePostProcessor.class)
                .stream()
                .forEach(processor -> processor.postProcess(scene));

        lightDi.getBean(UiInitializer.class).initialize();

        stage.show();
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

    private VBox createIcon(String effectId, String name, String file) {
        ImageView image = new ImageView(file);
        image.setPreserveRatio(true);
        image.setFitWidth(50);
        Label text = new Label();
        text.setText(name);

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
            content.putString(effectId);
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
        uiTimeline = lightDi.getBean(UiTimeline.class);
        uiTimelineManager = lightDi.getBean(UiTimelineManager.class);
        effectPropertyView = lightDi.getBean(PropertyView.class);
        uiTimelineManager.registerUiConsumer(position -> uiTimeline.updateLine(position));
        uiTimelineManager.registerUiConsumer(position -> effectPropertyView.updateValues(position));
        uiTimelineManager.registerUiConsumer(position -> updateTime(position));
        displayUpdateService = lightDi.getBean(DisplayUpdaterService.class);
        uiTimelineManager.registerConsumer(position -> displayUpdateService.updateDisplay(position));
        uiProjectRepository = lightDi.getBean(UiProjectRepository.class);
        renderService = lightDi.getBean(UiRenderService.class);
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

    public void launchUi() {
        launch();
    }

}