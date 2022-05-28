package com.helospark.tactview.ui.javafx.tabs.dockabletab.impl;

import java.math.BigDecimal;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.CanvasStateHolder;
import com.helospark.tactview.ui.javafx.CanvasStates;
import com.helospark.tactview.ui.javafx.UiPlaybackPreferenceRepository;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.UiTimelineManagerFactory;
import com.helospark.tactview.ui.javafx.render.SingleFullImageViewController;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.uicomponents.ScaleComboBoxFactory;
import com.helospark.tactview.ui.javafx.uicomponents.audiocomponent.AudioVisualizationComponent;
import com.helospark.tactview.ui.javafx.uicomponents.audiocomponent.AudioVisualizationComponentFactory;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public abstract class AbstractPreviewDockableTabFactory extends AbstractCachingDockableTabFactory {
    private UiPlaybackPreferenceRepository playbackPreferenceRepository;
    private UiProjectRepository uiProjectRepository;
    private UiTimelineManager uiTimelineManager;
    private SingleFullImageViewController fullScreenRenderer;
    private ScaleComboBoxFactory scaleComboBoxFactory;
    private MessagingService messagingService;
    private CanvasStates canvasStates;
    private AudioVisualizationComponentFactory audioVisualizationComponentFactory;
    private UiTimelineManagerFactory uiTimelineManagerFactory;

    public Label videoTimestampLabel;
    private String id;
    private CanvasStateHolder canvasStateHolder;

    public AbstractPreviewDockableTabFactory(String id, UiPlaybackPreferenceRepository playbackPreferenceRepository,
            UiProjectRepository uiProjectRepository, AudioVisualizationComponentFactory audioVisualizationComponentFactory,
            SingleFullImageViewController fullScreenRenderer, ScaleComboBoxFactory scaleComboBoxFactory,
            MessagingService messagingService, CanvasStates canvasStates, UiTimelineManagerFactory uiTimelineManagerFactory) {
        this.playbackPreferenceRepository = playbackPreferenceRepository;
        this.uiProjectRepository = uiProjectRepository;
        this.fullScreenRenderer = fullScreenRenderer;
        this.scaleComboBoxFactory = scaleComboBoxFactory;
        this.messagingService = messagingService;
        this.id = id;
        this.canvasStates = canvasStates;
        this.audioVisualizationComponentFactory = audioVisualizationComponentFactory;
        this.uiTimelineManagerFactory = uiTimelineManagerFactory;
    }

    protected BorderPane createPreviewRightVBox() {
        canvasStateHolder = new CanvasStateHolder(messagingService);
        Canvas canvas = new Canvas();
        canvasStateHolder.setCanvas(canvas);
        canvasStates.registerCanvas(id, canvasStateHolder);
        uiTimelineManager = uiTimelineManagerFactory.create(canvasStateHolder);
        BorderPane previewScrollPane = createCentered(canvas, canvasStateHolder);
        canvas.widthProperty().bind(previewScrollPane.widthProperty().subtract(20.0));
        canvas.heightProperty().bind(previewScrollPane.heightProperty().subtract(30.0));

        AudioVisualizationComponent audioVisualazationComponent = audioVisualizationComponentFactory.create(canvasStateHolder);

        VBox rightVBox = new VBox(3);
        rightVBox.setAlignment(Pos.TOP_CENTER);
        rightVBox.setId("clip-view");
        rightVBox.getChildren().add(previewScrollPane);
        rightVBox.getChildren().add(audioVisualazationComponent.getCanvas());
        VBox.setVgrow(previewScrollPane, Priority.ALWAYS);
        audioVisualazationComponent.clearCanvas();

        videoTimestampLabel = new Label("00:00:00.000");
        videoTimestampLabel.setId("video-timestamp-label");
        HBox videoStatusBar = new HBox(10);
        videoStatusBar.setId("video-status-bar");
        videoStatusBar.getChildren().add(videoTimestampLabel);
        rightVBox.getChildren().add(videoStatusBar);

        HBox underVideoBar = new HBox(1);
        ToggleButton muteButton = new ToggleButton("", new Glyph("FontAwesome",
                FontAwesome.Glyph.VOLUME_OFF));
        muteButton.setSelected(false);
        muteButton.setOnAction(event -> playbackPreferenceRepository.setMute(muteButton.isSelected()));
        muteButton.setTooltip(new Tooltip("Mute"));

        Button maximalFrameButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.IMAGE));
        maximalFrameButton.setOnMouseClicked(e -> fullScreenRenderer.renderFullScreenAtCurrentLocation());
        maximalFrameButton.setTooltip(new Tooltip("Show maximal preview frame"));

        Button fullscreenButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.EXPAND));
        fullscreenButton.setOnMouseClicked(e -> uiTimelineManager.startFullscreenPlayback());
        fullscreenButton.setTooltip(new Tooltip("Fullscreen"));

        ToggleButton halfImageEffectButton = new ToggleButton("", new Glyph("FontAwesome", FontAwesome.Glyph.STAR_HALF_ALT));
        halfImageEffectButton.setSelected(false);
        halfImageEffectButton.setOnAction(e -> {
            playbackPreferenceRepository.setHalfEffect(halfImageEffectButton.isSelected());
            uiTimelineManager.refreshDisplay(true);
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

        ComboBox<String> sizeDropDown = scaleComboBoxFactory.create(canvasStateHolder);

        ComboBox<String> playbackSpeedDropDown = new ComboBox<>();
        playbackSpeedDropDown.getStyleClass().add("size-drop-down");
        playbackSpeedDropDown.getItems().add("0.5x");
        playbackSpeedDropDown.getItems().add("0.75x");
        playbackSpeedDropDown.getItems().add("1.0x");
        playbackSpeedDropDown.getItems().add("1.25x");
        playbackSpeedDropDown.getItems().add("1.5x");
        playbackSpeedDropDown.getItems().add("2.0x");
        playbackSpeedDropDown.getSelectionModel().select("1.0x");
        playbackSpeedDropDown.setTooltip(new Tooltip("Playback speed"));
        playbackSpeedDropDown.valueProperty().addListener((o, oldValue, newValue2) -> {
            playbackPreferenceRepository.setPlaybackSpeedMultiplier(new BigDecimal(newValue2.replace("x", "")));
        });

        underVideoBar.getChildren().add(sizeDropDown);
        underVideoBar.getChildren().add(muteButton);
        underVideoBar.getChildren().add(halfImageEffectButton);
        underVideoBar.getChildren().add(createSmallEmptyGap());
        underVideoBar.getChildren().add(jumpBackButton);
        underVideoBar.getChildren().add(jumpBackOnFrameButton);
        underVideoBar.getChildren().add(playButton);
        underVideoBar.getChildren().add(stopButton);
        underVideoBar.getChildren().add(jumpForwardOnFrameButton);
        underVideoBar.getChildren().add(jumpForwardButton);
        underVideoBar.getChildren().add(createSmallEmptyGap());
        underVideoBar.getChildren().add(maximalFrameButton);
        underVideoBar.getChildren().add(fullscreenButton);
        underVideoBar.getChildren().add(playbackSpeedDropDown);
        underVideoBar.setId("video-button-bar");
        rightVBox.getChildren().add(underVideoBar);

        BorderPane rightBorderPane = new BorderPane();
        rightBorderPane.setCenter(rightVBox);

        uiTimelineManager.registerUiPlaybackConsumer(position -> updateTime(position));
        return rightBorderPane;
    }

    private Node createSmallEmptyGap() {
        HBox hbox = new HBox();
        hbox.getStyleClass().add("small-empty-space");
        return hbox;
    }

    private void updateTime(TimelinePosition position) {
        long wholePartOfTime = position.getSeconds().longValue();
        long hours = wholePartOfTime / 3600;
        long minutes = (wholePartOfTime - hours * 3600) / 60;
        long seconds = (wholePartOfTime - hours * 3600 - minutes * 60);
        long millis = position.getSeconds().subtract(BigDecimal.valueOf(wholePartOfTime)).multiply(BigDecimal.valueOf(1000)).longValue();

        String newLabel = String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);

        videoTimestampLabel.setText(newLabel);
    }

    private BorderPane createCentered(Canvas canvas2, CanvasStateHolder canvasStateHolder) {
        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("preview-canvas-border-pane");
        borderPane.setCenter(canvas2);

        ScrollBar rightScroll = new ScrollBar();
        rightScroll.setOrientation(Orientation.VERTICAL);
        canvasStateHolder.getTranslateYProperty().bindBidirectional(rightScroll.valueProperty());

        ScrollBar bottomScroll = new ScrollBar();

        canvasStateHolder.getTranslateXProperty().bindBidirectional(bottomScroll.valueProperty());

        borderPane.setRight(rightScroll);
        borderPane.setBottom(bottomScroll);
        rightScroll.setMin(-2000);
        rightScroll.setMax(2000);
        rightScroll.setUnitIncrement(100);
        rightScroll.setBlockIncrement(100);
        rightScroll.setVisibleAmount(500);
        bottomScroll.setMin(-2000);
        bottomScroll.setMax(2000);
        bottomScroll.setUnitIncrement(100);
        bottomScroll.setBlockIncrement(100);
        bottomScroll.setVisibleAmount(500);

        uiProjectRepository.getPreviewWidthProperty().addListener((listener, oldValue, newValue) -> {
            showBottomScrollBarIfNeeded(bottomScroll, newValue, canvasStateHolder);
        });
        uiProjectRepository.getPreviewHeightProperty().addListener((listener, oldValue, newValue) -> {
            showRightScrollBarIfNeeded(rightScroll, newValue, canvasStateHolder);
        });

        return borderPane;
    }

    private void showRightScrollBarIfNeeded(ScrollBar rightScroll, Number newValue, CanvasStateHolder canvasStateHolder) {
        if (canvasStateHolder.getCanvas().getHeight() < newValue.doubleValue()) {
            rightScroll.setMin(-1.0 * newValue.doubleValue());
            rightScroll.setMax(1.0 * newValue.doubleValue());
            setVisible(rightScroll, true);
        } else {
            setVisible(rightScroll, false);
        }
    }

    private void showBottomScrollBarIfNeeded(ScrollBar bottomScroll, Number newValue, CanvasStateHolder canvasStateHolder) {
        if (canvasStateHolder.getCanvas().getWidth() < newValue.doubleValue()) {
            bottomScroll.setMin(-1.0 * newValue.doubleValue());
            bottomScroll.setMax(1.0 * newValue.doubleValue());
            setVisible(bottomScroll, true);
        } else {
            setVisible(bottomScroll, false);
        }
    }

    private void setVisible(Node node, boolean value) {
        node.setVisible(value);
        node.setManaged(value);
    }

    @Override
    public boolean doesSupport(String id) {
        return this.id.equals(id);
    }

    @Override
    public String getId() {
        return this.id;
    }

    public CanvasStateHolder getCanvasStateHolder() {
        return canvasStateHolder;
    }

    public UiTimelineManager getUiTimelineManager() {
        return uiTimelineManager;
    }

}
