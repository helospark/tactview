package com.helospark.tactview.ui.javafx.tabs.dockabletab.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.init.PostInitializationArgsCallback;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.GlobalDirtyClipManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.CanvasStateHolder;
import com.helospark.tactview.ui.javafx.DisplayUpdaterService;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.PlaybackFrameAccessor;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.UiPlaybackManager;
import com.helospark.tactview.ui.javafx.UiPlaybackPreferenceRepository;
import com.helospark.tactview.ui.javafx.audio.AudioStreamService;
import com.helospark.tactview.ui.javafx.audio.JavaByteArrayConverter;
import com.helospark.tactview.ui.javafx.render.SingleFullImageViewController;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.uicomponents.DefaultCanvasTranslateSetter;
import com.helospark.tactview.ui.javafx.uicomponents.ScaleComboBoxFactory;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;
import com.helospark.tactview.ui.javafx.uicomponents.audiocomponent.AudioVisualizationComponent;
import com.helospark.tactview.ui.javafx.uicomponents.display.AudioPlayedListener;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedListener;
import com.helospark.tactview.ui.javafx.uicomponents.util.AudioRmsCalculator;

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

@Component
public class PreviewDockableTab extends AbstractCachingDockableTabFactory implements PostInitializationArgsCallback {
    public static final String ID = "preview";
    private UiPlaybackPreferenceRepository playbackPreferenceRepository;
    private AudioVisualizationComponent audioVisualazationComponent;
    private UiProjectRepository uiProjectRepository;
    private UiPlaybackManager uiPlaybackManager;
    private SingleFullImageViewController fullScreenRenderer;
    private ScaleComboBoxFactory scaleComboBoxFactory;
    private CanvasStateHolder canvasStateHolder;
    private GlobalTimelinePositionHolder globalTimelinePositionHolder;
    private DisplayUpdaterService displayUpdaterService;

    public Label videoTimestampLabel;
    private Canvas canvas;

    public PreviewDockableTab(UiProjectRepository uiProjectRepository,
            SingleFullImageViewController fullScreenRenderer,
            GlobalTimelinePositionHolder globalTimelinePositionHolder,

            MessagingService messagingService,

            ProjectRepository projectRepository, TimelineState timelineState, PlaybackFrameAccessor playbackController,
            AudioStreamService audioStreamService, JavaByteArrayConverter javaByteArrayConverter,
            List<AudioPlayedListener> audioPlayedListeners, @Qualifier("generalTaskScheduledService") ScheduledExecutorService scheduledExecutorService,

            GlobalDirtyClipManager globalDirtyClipManager, List<DisplayUpdatedListener> displayUpdateListeners,
            SelectedNodeRepository selectedNodeRepository,

            CanvasStateHolder canvasStateHolder, AudioRmsCalculator audioRmsCalculator,

            UiMessagingService uiMessagingService, DefaultCanvasTranslateSetter defaultCanvasTranslateSetter) {
        this.uiProjectRepository = uiProjectRepository;
        this.fullScreenRenderer = fullScreenRenderer;
        this.globalTimelinePositionHolder = globalTimelinePositionHolder;

        this.canvasStateHolder = canvasStateHolder;

        // instance specific data
        this.canvas = new Canvas();
        canvasStateHolder.setCanvas(canvas);

        this.scaleComboBoxFactory = new ScaleComboBoxFactory(projectRepository, uiProjectRepository, uiMessagingService, defaultCanvasTranslateSetter, canvasStateHolder);

        this.audioVisualazationComponent = new AudioVisualizationComponent(playbackController, audioRmsCalculator, canvas);

        this.playbackPreferenceRepository = new UiPlaybackPreferenceRepository();

        this.displayUpdaterService = new DisplayUpdaterService(playbackController, uiProjectRepository, globalDirtyClipManager, displayUpdateListeners,
                messagingService, scheduledExecutorService, selectedNodeRepository, canvasStateHolder, globalTimelinePositionHolder, playbackPreferenceRepository);
        this.displayUpdaterService.setCanvas(canvas);
        displayUpdaterService.init();

        List<AudioPlayedListener> newListeners = new ArrayList<>();
        newListeners.addAll(audioPlayedListeners);
        newListeners.add(audioVisualazationComponent);
        this.uiPlaybackManager = new UiPlaybackManager(projectRepository, timelineState, playbackController,
                audioStreamService, playbackPreferenceRepository, javaByteArrayConverter,
                newListeners, scheduledExecutorService, uiProjectRepository,
                globalTimelinePositionHolder, displayUpdaterService);
    }

    @Override
    public void call(String[] args) {
        this.audioVisualazationComponent.call(args);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    private BorderPane createPreviewRightVBox() {
        BorderPane rightBorderPane = new BorderPane();
        BorderPane previewScrollPane = createCentered(canvas);
        canvas.widthProperty().bind(rightBorderPane.widthProperty().subtract(20.0));
        canvas.heightProperty().bind(previewScrollPane.heightProperty().subtract(30.0));

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
        fullscreenButton.setOnMouseClicked(e -> uiPlaybackManager.startFullscreenPlayback());
        fullscreenButton.setTooltip(new Tooltip("Fullscreen"));

        ToggleButton halfImageEffectButton = new ToggleButton("", new Glyph("FontAwesome", FontAwesome.Glyph.STAR_HALF_ALT));
        halfImageEffectButton.setSelected(false);
        halfImageEffectButton.setOnAction(e -> {
            playbackPreferenceRepository.setHalfEffect(halfImageEffectButton.isSelected());
            uiPlaybackManager.refreshDisplay(true);
        });
        halfImageEffectButton.setTooltip(new Tooltip("Apply effects only on left side of preview"));

        Button playButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.PLAY));
        playButton.setOnMouseClicked(e -> uiPlaybackManager.startPlayback());
        playButton.setTooltip(new Tooltip("Play"));

        Button stopButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.STOP));
        stopButton.setOnMouseClicked(e -> uiPlaybackManager.stopPlayback());
        stopButton.setTooltip(new Tooltip("Stop"));

        Button jumpBackOnFrameButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.STEP_BACKWARD));
        jumpBackOnFrameButton.setOnMouseClicked(e -> globalTimelinePositionHolder.moveBackOneFrame());
        jumpBackOnFrameButton.setTooltip(new Tooltip("Step one frame back"));

        Button jumpForwardOnFrameButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.STEP_FORWARD));
        jumpForwardOnFrameButton.setOnMouseClicked(e -> globalTimelinePositionHolder.moveForwardOneFrame());
        jumpForwardOnFrameButton.setTooltip(new Tooltip("Step one frame forward"));

        Button jumpBackButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.BACKWARD));
        jumpBackButton.setOnMouseClicked(e -> globalTimelinePositionHolder.jumpRelative(BigDecimal.valueOf(-10)));
        jumpBackButton.setTooltip(new Tooltip("Step 10s back"));

        Button jumpForwardButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.FORWARD));
        jumpForwardButton.setOnMouseClicked(e -> globalTimelinePositionHolder.jumpRelative(BigDecimal.valueOf(10)));
        jumpForwardButton.setTooltip(new Tooltip("Step 10s forward"));

        ComboBox<String> sizeDropDown = scaleComboBoxFactory.create();

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

        rightBorderPane.setCenter(rightVBox);

        globalTimelinePositionHolder.registerUiPlaybackConsumer(position -> updateTime(position));
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

    private BorderPane createCentered(Canvas canvas2) {
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
            showBottomScrollBarIfNeeded(bottomScroll, newValue);
        });
        uiProjectRepository.getPreviewHeightProperty().addListener((listener, oldValue, newValue) -> {
            showRightScrollBarIfNeeded(rightScroll, newValue);
        });

        return borderPane;
    }

    private void showRightScrollBarIfNeeded(ScrollBar rightScroll, Number newValue) {
        if (canvasStateHolder.getCanvas().getHeight() < newValue.doubleValue()) {
            rightScroll.setMin(-1.0 * newValue.doubleValue());
            rightScroll.setMax(1.0 * newValue.doubleValue());
            setVisible(rightScroll, true);
        } else {
            setVisible(rightScroll, false);
        }
    }

    private void showBottomScrollBarIfNeeded(ScrollBar bottomScroll, Number newValue) {
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
    public DetachableTab createTabInternal() {
        return new DetachableTab("Preview", createPreviewRightVBox(), ID);
    }

    @Override
    public boolean doesSupport(String id) {
        return ID.equals(id);
    }

    @Override
    public String getId() {
        return ID;
    }

    public DisplayUpdaterService getDisplayUpdaterService() {
        return displayUpdaterService;
    }

}
