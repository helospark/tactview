package com.helospark.tactview.ui.javafx.tabs.dockabletab.impl;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.init.PostInitializationArgsCallback;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.GlobalDirtyClipManager;
import com.helospark.tactview.core.timeline.TimelineChannelsState;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.subtimeline.TimelineManagerAccessorFactory;
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
import com.helospark.tactview.ui.javafx.uicomponents.pattern.AudioImagePatternService;
import com.helospark.tactview.ui.javafx.uicomponents.util.AudioRmsCalculator;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.application.Platform;
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
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

@Component
public class TrimmingDockableTab extends AbstractCachingDockableTabFactory implements PostInitializationArgsCallback {
    public static final String ID = "trimmer";
    private UiPlaybackPreferenceRepository playbackPreferenceRepository;
    private AudioVisualizationComponent audioVisualazationComponent;
    private UiPlaybackManager uiPlaybackManager;
    private ScaleComboBoxFactory scaleComboBoxFactory;
    private CanvasStateHolder canvasStateHolder;
    private GlobalTimelinePositionHolder globalTimelinePositionHolder;
    private DisplayUpdaterService displayUpdaterService;
    private UiProjectRepository uiProjectRepository;

    public Label videoTimestampLabel;
    private Canvas canvas;
    private Canvas timelinePatternCanvas;

    private TimelineChannelsState timelineChannelsState;
    private TimelineManagerAccessor timelineManagerAccessor;
    private TimelineManagerRenderService timelineManagerRenderService;
    private AudioImagePatternService audioImagePatternService;
    private ScheduledExecutorService scheduledExecutorService;

    private TimelinePosition trimStartPosition = TimelinePosition.ofZero();
    private TimelinePosition trimEndPosition = TimelinePosition.ofZero();

    public TrimmingDockableTab(SingleFullImageViewController fullScreenRenderer,

            MessagingService messagingService,

            ProjectRepository projectRepository, TimelineState timelineState,
            AudioStreamService audioStreamService, JavaByteArrayConverter javaByteArrayConverter,
            List<AudioPlayedListener> audioPlayedListeners, @Qualifier("generalTaskScheduledService") ScheduledExecutorService scheduledExecutorService,

            GlobalDirtyClipManager globalDirtyClipManager, List<DisplayUpdatedListener> displayUpdateListeners,
            SelectedNodeRepository selectedNodeRepository,

            AudioRmsCalculator audioRmsCalculator,
            @Qualifier("getterIgnoringObjectMapper") ObjectMapper objectMapper,
            UiMessagingService uiMessagingService,
            ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter,
            TimelineManagerAccessorFactory timelineManagerAccessor,
            AudioImagePatternService audioImagePatternService) {
        this.audioImagePatternService = audioImagePatternService;
        this.scheduledExecutorService = scheduledExecutorService;

        // instance specific data
        this.globalTimelinePositionHolder = new GlobalTimelinePositionHolder(projectRepository);
        this.uiProjectRepository = new UiProjectRepository(objectMapper);

        this.canvas = new Canvas();
        this.canvasStateHolder = new CanvasStateHolder(messagingService);
        this.canvasStateHolder.setCanvas(canvas);

        DefaultCanvasTranslateSetter defaultCanvasTranslateSetter = new DefaultCanvasTranslateSetter(canvasStateHolder);
        this.scaleComboBoxFactory = new ScaleComboBoxFactory(projectRepository, uiProjectRepository, uiMessagingService, defaultCanvasTranslateSetter, canvasStateHolder);

        createTimeline(timelineManagerAccessor);

        PlaybackFrameAccessor playbackController = new PlaybackFrameAccessor(timelineManagerRenderService, uiProjectRepository, projectRepository, byteBufferToJavaFxImageConverter);

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

    private void createTimeline(TimelineManagerAccessorFactory timelineManagerAccessorFactory) {
        this.timelineChannelsState = new TimelineChannelsState();
        this.timelineManagerAccessor = timelineManagerAccessorFactory.createAccessor(timelineChannelsState);
        for (int i = 0; i < 2; ++i) {
            timelineManagerAccessor.createChannel(i);
        }
        this.timelineManagerRenderService = timelineManagerAccessorFactory.createRenderService(timelineChannelsState, timelineManagerAccessor);
    }

    @Override
    public void call(String[] args) {
        this.audioVisualazationComponent.call(args);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    private BorderPane createPreviewRightVBox() {
        BorderPane previewScrollPane = createCentered(canvas);
        canvas.widthProperty().bind(previewScrollPane.widthProperty().subtract(20.0));
        canvas.heightProperty().bind(previewScrollPane.heightProperty().subtract(30.0));

        canvas.setOnDragOver(event -> {
            Dragboard dragBoard = event.getDragboard();
            List<File> files = dragBoard.getFiles();
            if (files.size() > 0) {
                timelineManagerAccessor.removeAllClips();
                AddClipRequest addClipRequest = AddClipRequest.builder()
                        .withChannelId(timelineManagerAccessor.findChannelOnIndex(0).get().getId())
                        .withFilePath(files.get(0).getAbsolutePath())
                        .withPosition(TimelinePosition.ofZero())
                        .withProceduralClipId(null)
                        .build();
                List<TimelineClip> clips = timelineManagerAccessor.addClip(addClipRequest);
                this.displayUpdaterService.updateCurrentPositionWithInvalidatedCache();

                trimStartPosition = TimelinePosition.ofZero();
                trimEndPosition = clips.get(0).getInterval().getEndPosition();

                redrawPattern();
            }
        });

        VBox rightVBox = new VBox(3);
        rightVBox.setAlignment(Pos.TOP_CENTER);
        rightVBox.setId("clip-view");
        rightVBox.getChildren().add(previewScrollPane);
        rightVBox.getChildren().add(audioVisualazationComponent.getCanvas());
        VBox.setVgrow(previewScrollPane, Priority.ALWAYS);
        audioVisualazationComponent.clearCanvas();

        timelinePatternCanvas = createTimelineCanvas();
        rightVBox.getChildren().add(timelinePatternCanvas);

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

        Button fullscreenButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.EXPAND));
        fullscreenButton.setOnMouseClicked(e -> uiPlaybackManager.startFullscreenPlayback());
        fullscreenButton.setTooltip(new Tooltip("Fullscreen"));

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
        underVideoBar.getChildren().add(createSmallEmptyGap());
        underVideoBar.getChildren().add(jumpBackButton);
        underVideoBar.getChildren().add(jumpBackOnFrameButton);
        underVideoBar.getChildren().add(playButton);
        underVideoBar.getChildren().add(stopButton);
        underVideoBar.getChildren().add(jumpForwardOnFrameButton);
        underVideoBar.getChildren().add(jumpForwardButton);
        underVideoBar.getChildren().add(createSmallEmptyGap());
        underVideoBar.getChildren().add(fullscreenButton);
        underVideoBar.getChildren().add(playbackSpeedDropDown);
        underVideoBar.setId("video-button-bar");
        rightVBox.getChildren().add(underVideoBar);

        BorderPane rightBorderPane = new BorderPane();
        rightBorderPane.setCenter(rightVBox);

        globalTimelinePositionHolder.registerUiPlaybackConsumer(position -> updateTime(position));
        return rightBorderPane;
    }

    private Canvas createTimelineCanvas() {
        Canvas timelineCanvas = new Canvas(100, 30);

        canvas.widthProperty().addListener((e, oldV, newV) -> {
            if (newV.doubleValue() <= 0.0) {
                return;
            }
            timelineCanvas.widthProperty().set(newV.doubleValue());

            clearCanvas(timelineCanvas);
        });
        return timelineCanvas;
    }

    private void redrawPattern() {
        scheduledExecutorService.execute(() -> {
            List<String> allClips = timelineManagerAccessor.getAllClipIds();

            AudibleTimelineClip clip = null;
            for (var clipId : allClips) {
                TimelineClip currentClip = timelineManagerAccessor.findClipById(clipId).get();
                if (currentClip.getType().equals(TimelineClipType.AUDIO)) {
                    clip = (AudibleTimelineClip) currentClip;
                }
            }

            if (clip != null) {
                double endPosition = clip.getInterval().getLength().getSeconds().doubleValue();
                Image image = audioImagePatternService.createAudioImagePattern(clip, (int) timelinePatternCanvas.getWidth(), (int) timelinePatternCanvas.getHeight(), 0.0, endPosition);
                Platform.runLater(() -> {
                    timelinePatternCanvas.getGraphicsContext2D().drawImage(image, 0, 0);
                });
            }
        });
    }

    private void clearCanvas(Canvas timelineCanvas) {
        timelineCanvas.getGraphicsContext2D().fillRect(0, 0, timelineCanvas.getWidth(), timelineCanvas.getHeight());
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
        return new DetachableTab("Trimmer", createPreviewRightVBox(), ID);
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
