package com.helospark.tactview.ui.javafx.tabs.dockabletab.impl;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.init.PostInitializationArgsCallback;
import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.AddExistingClipRequest;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.ClipChannelPair;
import com.helospark.tactview.core.timeline.GlobalDirtyClipManager;
import com.helospark.tactview.core.timeline.LinkClipRepository;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineChannelsState;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.core.timeline.subtimeline.TimelineManagerAccessorFactory;
import com.helospark.tactview.core.util.AudioRmsCalculator;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.CanvasStateHolder;
import com.helospark.tactview.ui.javafx.DisplayUpdaterService;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.PlaybackFrameAccessor;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.UiPlaybackManager;
import com.helospark.tactview.ui.javafx.UiPlaybackPreferenceRepository;
import com.helospark.tactview.ui.javafx.audio.AudioStreamService;
import com.helospark.tactview.ui.javafx.audio.JavaByteArrayConverter;
import com.helospark.tactview.ui.javafx.commands.impl.AddExistingClipsCommand;
import com.helospark.tactview.ui.javafx.render.SingleFullImageViewController;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.scenepostprocessor.GlobalKeyCombinationAttacher;
import com.helospark.tactview.ui.javafx.script.ScriptVariablesStore;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.impl.trimmer.CacheableAudioImagePatternService;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.uicomponents.DefaultCanvasTranslateSetter;
import com.helospark.tactview.ui.javafx.uicomponents.ScaleComboBoxFactory;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;
import com.helospark.tactview.ui.javafx.uicomponents.audiocomponent.AudioVisualizationComponent;
import com.helospark.tactview.ui.javafx.uicomponents.display.AudioPlayedListener;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedListener;
import com.helospark.tactview.ui.javafx.uicomponents.window.ProjectMediaWindow;
import com.helospark.tactview.ui.javafx.uicomponents.window.projectmedia.ProjectMediaElement;
import com.helospark.tactview.ui.javafx.uicomponents.window.projectmedia.ThumbnailCreator;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

@Component
public class TrimmingDockableTab extends AbstractCachingDockableTabFactory implements PostInitializationArgsCallback, ResettableBean {
    public static final String TRIMMING_MEDIA_ENTRY = "trimming-media-entry";
    public static final String ID = "trimmer";
    private UiPlaybackPreferenceRepository playbackPreferenceRepository;
    private AudioVisualizationComponent audioVisualazationComponent;
    private UiPlaybackManager uiPlaybackManager;
    private ScaleComboBoxFactory scaleComboBoxFactory;
    private CanvasStateHolder canvasStateHolder;
    private GlobalTimelinePositionHolder trimmerTimelinePositionHolder;
    private DisplayUpdaterService displayUpdaterService;
    private UiProjectRepository uiProjectRepository;
    private ProjectRepository projectRepository;
    private TimelineManagerAccessor globalTimelineManagerAccessor;
    private UiCommandInterpreterService commandInterpreterService;
    private LinkClipRepository linkClipRepository;
    private ThumbnailCreator thumbnailCreator;
    private ScriptVariablesStore scriptVariablesStore;

    public Label videoTimestampLabel;
    private Canvas canvas;
    private Canvas timelinePatternCanvas;

    private TimelineChannelsState trimmerTimelineChannelsState;
    private TimelineManagerAccessor trimmerTimelineManagerAccessor;
    private TimelineManagerRenderService trimmerTimelineManagerRenderService;
    private CacheableAudioImagePatternService audioImagePatternService;
    private ScheduledExecutorService scheduledExecutorService;
    private ProjectMediaWindow projectMediaWindow;
    private TimelineState timelineState;

    private TimelinePosition trimStartPosition = null;
    private TimelinePosition trimEndPosition = null;
    private double pixelsPerSeconds = 1.0;
    private String fileName = "";
    private DefaultCanvasTranslateSetter defaultCanvasTranslateSetter;

    ContextMenu contextMenu = new ContextMenu();

    public TrimmingDockableTab(SingleFullImageViewController fullScreenRenderer,
            MessagingService messagingService,
            ScriptVariablesStore scriptVariablesStore,
            GlobalKeyCombinationAttacher globalKeyCombinationAttacher,

            AudioStreamService audioStreamService, JavaByteArrayConverter javaByteArrayConverter,
            List<AudioPlayedListener> audioPlayedListeners,
            @Qualifier("generalTaskScheduledService") ScheduledExecutorService scheduledExecutorService,

            GlobalDirtyClipManager globalDirtyClipManager,
            List<DisplayUpdatedListener> displayUpdateListeners,
            SelectedNodeRepository selectedNodeRepository,

            AudioRmsCalculator audioRmsCalculator,
            @Qualifier("getterIgnoringObjectMapper") ObjectMapper objectMapper,
            UiMessagingService uiMessagingService,
            ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter,
            TimelineManagerAccessorFactory timelineManagerAccessor,
            CacheableAudioImagePatternService audioImagePatternService,
            ProjectMediaWindow projectMediaWindow,
            DefaultCanvasTranslateSetter defaultCanvasTranslateSetter,
            TimelineManagerAccessor globalTimelineManagerAccessor,
            UiCommandInterpreterService commandInterpreterService,
            LinkClipRepository linkClipRepository,
            ThumbnailCreator thumbnailCreator) {
        this.audioImagePatternService = audioImagePatternService;
        this.scheduledExecutorService = scheduledExecutorService;
        this.projectMediaWindow = projectMediaWindow;
        this.defaultCanvasTranslateSetter = defaultCanvasTranslateSetter;
        this.globalTimelineManagerAccessor = globalTimelineManagerAccessor;
        this.commandInterpreterService = commandInterpreterService;
        this.linkClipRepository = linkClipRepository;
        this.thumbnailCreator = thumbnailCreator;

        // instance specific data
        projectRepository = new ProjectRepository(objectMapper, messagingService);
        this.trimmerTimelinePositionHolder = new GlobalTimelinePositionHolder(projectRepository);
        trimmerTimelinePositionHolder.registerUiPlaybackConsumer(time -> {
            redrawTimelineCanvas();
        });
        this.uiProjectRepository = new UiProjectRepository(objectMapper);

        this.canvas = new Canvas();
        this.canvasStateHolder = new CanvasStateHolder(messagingService);
        this.canvasStateHolder.setCanvas(canvas);

        this.scaleComboBoxFactory = new ScaleComboBoxFactory(projectRepository, uiProjectRepository, uiMessagingService, defaultCanvasTranslateSetter, canvasStateHolder);

        createTimeline(timelineManagerAccessor);

        PlaybackFrameAccessor playbackController = new PlaybackFrameAccessor(trimmerTimelineManagerRenderService, uiProjectRepository, projectRepository, byteBufferToJavaFxImageConverter,
                scriptVariablesStore);

        this.audioVisualazationComponent = new AudioVisualizationComponent(playbackController, audioRmsCalculator, canvas);

        this.playbackPreferenceRepository = new UiPlaybackPreferenceRepository();

        this.displayUpdaterService = new DisplayUpdaterService(playbackController, uiProjectRepository, globalDirtyClipManager, displayUpdateListeners,
                messagingService, scheduledExecutorService, selectedNodeRepository, canvasStateHolder, trimmerTimelinePositionHolder, playbackPreferenceRepository,
                globalTimelineManagerAccessor);
        this.displayUpdaterService.setCanvas(canvas);
        displayUpdaterService.init();

        List<AudioPlayedListener> newListeners = new ArrayList<>();
        newListeners.addAll(audioPlayedListeners);
        newListeners.add(audioVisualazationComponent);
        timelineState = new TimelineState();
        this.uiPlaybackManager = new UiPlaybackManager(projectRepository, timelineState, playbackController,
                audioStreamService, playbackPreferenceRepository, javaByteArrayConverter,
                newListeners, scheduledExecutorService, uiProjectRepository,
                trimmerTimelinePositionHolder, displayUpdaterService, globalKeyCombinationAttacher);
    }

    private void createTimeline(TimelineManagerAccessorFactory timelineManagerAccessorFactory) {
        this.trimmerTimelineChannelsState = new TimelineChannelsState();
        this.trimmerTimelineManagerAccessor = timelineManagerAccessorFactory.createAccessor(trimmerTimelineChannelsState);
        for (int i = 0; i < 2; ++i) {
            trimmerTimelineManagerAccessor.createChannel(i);
        }
        this.trimmerTimelineManagerRenderService = timelineManagerAccessorFactory.createRenderService(trimmerTimelineChannelsState, trimmerTimelineManagerAccessor);
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

        createDragOver();
        createContextMenu(previewScrollPane);

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
        jumpBackOnFrameButton.setOnMouseClicked(e -> trimmerTimelinePositionHolder.moveBackOneFrame());
        jumpBackOnFrameButton.setTooltip(new Tooltip("Step one frame back"));

        Button jumpForwardOnFrameButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.STEP_FORWARD));
        jumpForwardOnFrameButton.setOnMouseClicked(e -> trimmerTimelinePositionHolder.moveForwardOneFrame());
        jumpForwardOnFrameButton.setTooltip(new Tooltip("Step one frame forward"));

        Button jumpBackButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.BACKWARD));
        jumpBackButton.setOnMouseClicked(e -> trimmerTimelinePositionHolder.jumpRelative(BigDecimal.valueOf(-10)));
        jumpBackButton.setTooltip(new Tooltip("Step 10s back"));

        Button jumpForwardButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.FORWARD));
        jumpForwardButton.setOnMouseClicked(e -> trimmerTimelinePositionHolder.jumpRelative(BigDecimal.valueOf(10)));
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

        rightBorderPane.setCenter(rightVBox);

        trimmerTimelinePositionHolder.registerUiPlaybackConsumer(position -> updateTime(position));
        return rightBorderPane;
    }

    public void initializeProjectSize(int width, int height, BigDecimal fps) {
        double horizontalScaleFactor = (canvasStateHolder.getAvailableWidth()) / width;
        double verticalScaleFactor = (canvasStateHolder.getAvailableHeight()) / height;
        double scale = Math.min(horizontalScaleFactor, verticalScaleFactor);
        double aspectRatio = ((double) width) / ((double) height);
        int previewWidth = (int) (scale * width);
        int previewHeight = (int) (scale * height);
        uiProjectRepository.setScaleFactor(scale);
        uiProjectRepository.setAlignedPreviewSize(previewWidth, previewHeight, width, height);
        defaultCanvasTranslateSetter.setDefaultCanvasTranslate(canvasStateHolder, uiProjectRepository.getPreviewWidth(), uiProjectRepository.getPreviewHeight());
        uiProjectRepository.setAspectRatio(aspectRatio);

        projectRepository.initializeVideo(width, height, fps);
        // TODO: projectRepository.initializeAudio();
    }

    private void createDragOver() {
        canvas.setOnDragOver(event -> {
            Dragboard dragBoard = event.getDragboard();
            List<File> files = dragBoard.getFiles();
            if (files != null && files.size() > 0) {
                addFilesToTrimmer(files);
            }
        });
        canvas.setOnDragDetected(event -> {
            List<TimelineClip> clips = getClipsToCopy();
            Image image = thumbnailCreator.getImageFor(clips, trimStartPosition, 120);
            Dragboard db = canvas.startDragAndDrop(TransferMode.ANY);
            db.setDragView(image);

            ClipboardContent content = new ClipboardContent();
            content.putString(TRIMMING_MEDIA_ENTRY);
            ProjectMediaElement element = new ProjectMediaElement(UUID.randomUUID().toString(), clips, image, fileName);
            content.put(DataFormat.RTF, element);
            db.setContent(content);

            event.consume();
        });
    }

    private void addFilesToTrimmer(List<File> files) {
        File fileToLoad = files.get(0);
        trimmerTimelineManagerAccessor.removeAllClips();
        AddClipRequest addClipRequest = AddClipRequest.builder()
                .withChannelId(trimmerTimelineManagerAccessor.findChannelOnIndex(0).get().getId())
                .withFilePath(fileToLoad.getAbsolutePath())
                .withPosition(TimelinePosition.ofZero())
                .withProceduralClipId(null)
                .build();
        List<TimelineClip> clips = trimmerTimelineManagerAccessor.addClip(addClipRequest);

        if (clips.size() > 0) {
            TimelineInterval clipInterval = clips.get(0).getInterval();
            pixelsPerSeconds = timelinePatternCanvas.getWidth() / clipInterval.getLength().getSeconds().doubleValue();
            timelineState.setLoopAProperties(TimelinePosition.ofZero());
            timelineState.setLoopBProperties(clipInterval.getEndPosition());
        } else {
            timelineState.setLoopBProperties(TimelinePosition.ofSeconds(1));
        }
        fileName = fileToLoad.getName();

        this.displayUpdaterService.updateCurrentPositionWithInvalidatedCache();

        clearTrimIntervals(clips);

        VideoClip videoClip = clips.stream()
                .filter(clip -> clip instanceof VideoClip)
                .map(a -> (VideoClip) a)
                .findFirst()
                .orElse(null);
        if (videoClip != null) {
            VideoMetadata metadata = (VideoMetadata) videoClip.getMediaMetadata();
            initializeProjectSize(metadata.getWidth(), metadata.getHeight(), BigDecimal.valueOf(metadata.getFps()));
            displayUpdaterService.updateCurrentPositionWithInvalidatedCache();
        }
        redrawTimelineCanvas();
    }

    private void clearTrimIntervals(List<TimelineClip> clips) {
        trimStartPosition = TimelinePosition.ofZero();
        trimEndPosition = clips.get(0).getInterval().getEndPosition();
    }

    private void createContextMenu(BorderPane previewScrollPane) {
        canvas.setOnContextMenuRequested(e -> {
            contextMenu.hide();
            contextMenu.getItems().clear();

            MenuItem addToProjectMedia = createAddToProjectMediaMenuItem();
            contextMenu.getItems().add(addToProjectMedia);

            MenuItem addToTimeline = createAddToTimelineMenuItem();
            contextMenu.getItems().add(addToTimeline);

            contextMenu.getItems().add(new SeparatorMenuItem());

            MenuItem inPositionMenuItem = setInPositionContextMenu();
            contextMenu.getItems().add(inPositionMenuItem);

            MenuItem outPositionMenuItem = setOutPositionContextMenu();
            contextMenu.getItems().add(outPositionMenuItem);

            MenuItem clearTrimIntervalsMenuItem = createClearIntervalsMenuItem();
            contextMenu.getItems().add(clearTrimIntervalsMenuItem);

            contextMenu.show(previewScrollPane, e.getScreenX(), e.getScreenY());
            e.consume();
        });
        canvas.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                contextMenu.hide();
            }
        });

    }

    private MenuItem createAddToTimelineMenuItem() {
        MenuItem addToTimelineMenuItem = new MenuItem("Add to timeline");
        addToTimelineMenuItem.setOnAction(menuEvent -> {
            List<TimelineClip> clipsToAdd = getClipsToCopy();
            List<String> links = clipsToAdd.stream()
                    .map(a -> a.getId())
                    .collect(Collectors.toList());
            if (clipsToAdd.size() > 0) {

                TimelineClip firstClip = clipsToAdd.remove(0);

                List<ClipChannelPair> additionalClips = clipsToAdd.stream()
                        .map(clip -> new ClipChannelPair(clip, null))
                        .collect(Collectors.toList());

                TimelineChannel channel = globalTimelineManagerAccessor.getChannels().get(0);
                AddExistingClipRequest existingClipRequest = AddExistingClipRequest.builder()
                        .withChannel(channel)
                        .withPosition(Optional.empty())
                        .withClipToAdd(firstClip)
                        .withAdditionalClipsToAdd(additionalClips)
                        .build();
                commandInterpreterService.synchronousSend(new AddExistingClipsCommand(existingClipRequest, globalTimelineManagerAccessor));
                linkClipRepository.linkClips(links);
            }

        });
        return addToTimelineMenuItem;
    }

    private MenuItem createClearIntervalsMenuItem() {
        MenuItem clearTrimIntervalsMenuItem = new MenuItem("Clear in/out positions");
        clearTrimIntervalsMenuItem.setOnAction(menuEvent -> {
            clearTrimIntervals(getRawClipsCurrentlyUsed());
            redrawTimelineCanvas();
        });
        return clearTrimIntervalsMenuItem;
    }

    private MenuItem createAddToProjectMediaMenuItem() {
        MenuItem addToProjectMedia = new MenuItem("Add to project media");
        addToProjectMedia.setOnAction(menuEvent -> {
            List<TimelineClip> clipsToAdd = getClipsToCopy();
            if (clipsToAdd.size() > 0) {
                projectMediaWindow.addClips(clipsToAdd, fileName);
            }
        });
        return addToProjectMedia;
    }

    private TimelinePosition convertToTimePosition(double x) {
        return new TimelinePosition(x / pixelsPerSeconds);
    }

    private MenuItem setInPositionContextMenu() {
        MenuItem setInPosition = new MenuItem("Set in-position");
        setInPosition.setOnAction(menuEvent -> {
            TimelinePosition positionToSet = trimmerTimelinePositionHolder.getCurrentPosition();
            if (positionToSet.compareTo(trimEndPosition) == 0) {
                return;
            }

            if (trimEndPosition != null) {
                if (positionToSet.compareTo(trimEndPosition) < 0) {
                    trimStartPosition = positionToSet;
                } else {
                    trimStartPosition = trimEndPosition;
                    trimEndPosition = positionToSet;
                }
            } else {
                trimStartPosition = positionToSet;
            }
            redrawTimelineCanvas();
        });
        return setInPosition;
    }

    private MenuItem setOutPositionContextMenu() {
        MenuItem setOutPosition = new MenuItem("Set out-position");
        setOutPosition.setOnAction(menuEvent -> {
            TimelinePosition positionToSet = trimmerTimelinePositionHolder.getCurrentPosition();
            if (positionToSet.compareTo(trimEndPosition) == 0) {
                return;
            }

            if (trimStartPosition != null) {
                if (positionToSet.compareTo(trimStartPosition) > 0) {
                    trimEndPosition = positionToSet;
                } else {
                    trimEndPosition = trimStartPosition;
                    trimStartPosition = positionToSet;
                }
            } else {
                trimEndPosition = positionToSet;
            }
            redrawTimelineCanvas();
        });
        return setOutPosition;
    }

    private List<TimelineClip> getClipsToCopy() {
        List<TimelineClip> clipsToAdd = trimmerTimelineManagerAccessor.getAllClipIds()
                .stream()
                .flatMap(a -> trimmerTimelineManagerAccessor.findClipById(a).stream())
                .map(a -> a.cloneClip(CloneRequestMetadata.ofDefault()))
                .collect(Collectors.toList());
        List<TimelineClip> result = new ArrayList<>();
        if (trimStartPosition != null && trimEndPosition != null) {
            for (var clip : clipsToAdd) {
                List<TimelineClip> firstCut = clip.createCutClipParts(trimStartPosition);
                TimelineClip secondPart;
                if (firstCut.size() > 1) {
                    secondPart = firstCut.get(1);
                } else {
                    secondPart = clip;
                }
                List<TimelineClip> secondCut = secondPart.createCutClipParts(trimEndPosition);
                TimelineClip cutPart = secondCut.get(0);
                result.add(cutPart);
            }
        } else {
            result.addAll(clipsToAdd);
        }
        return result;
    }

    private List<TimelineClip> getRawClipsCurrentlyUsed() {
        return trimmerTimelineManagerAccessor.getAllClipIds()
                .stream()
                .flatMap(a -> trimmerTimelineManagerAccessor.findClipById(a).stream())
                .collect(Collectors.toList());
    }

    private Canvas createTimelineCanvas() {
        Canvas timelineCanvas = new Canvas(100, 30);

        canvas.widthProperty().addListener((e, oldV, newV) -> {
            if (newV.doubleValue() <= 0.0) {
                return;
            }
            timelineCanvas.widthProperty().set(newV.doubleValue() - 20);

            List<TimelineClip> clips = getRawClipsCurrentlyUsed();
            if (clips.size() > 0) {
                pixelsPerSeconds = timelineCanvas.getWidth() / clips.get(0).getInterval().getLength().getSeconds().doubleValue();
            }

            redrawTimelineCanvas();
        });

        timelineCanvas.setOnMouseClicked(e -> {
            TimelinePosition position = convertToTimePosition(e.getX());
            trimmerTimelinePositionHolder.jumpAbsolute(position);
            redrawTimelineCanvas();
        });
        timelineCanvas.setOnMouseDragged(e -> {
            TimelinePosition position = convertToTimePosition(e.getX());
            trimmerTimelinePositionHolder.jumpAbsolute(position);
            redrawTimelineCanvas();
        });

        return timelineCanvas;
    }

    private void redrawTimelineCanvas() {
        scheduledExecutorService.execute(() -> {
            List<String> allClips = trimmerTimelineManagerAccessor.getAllClipIds();

            AudibleTimelineClip clip = null;
            for (var clipId : allClips) {
                TimelineClip currentClip = trimmerTimelineManagerAccessor.findClipById(clipId).get();
                if (currentClip.getType().equals(TimelineClipType.AUDIO)) {
                    clip = (AudibleTimelineClip) currentClip;
                }
            }

            if (clip != null) {
                double endPosition = clip.getInterval().getLength().getSeconds().doubleValue();
                Image image = audioImagePatternService.createAudioImagePattern(clip, (int) timelinePatternCanvas.getWidth(), (int) timelinePatternCanvas.getHeight(), 0.0, endPosition);
                Platform.runLater(() -> {
                    GraphicsContext graphics = timelinePatternCanvas.getGraphicsContext2D();
                    graphics.drawImage(image, 0, 0, timelinePatternCanvas.getWidth(), timelinePatternCanvas.getHeight());
                });
            } else {
                clearTimelineCanvas(timelinePatternCanvas);
            }
            Platform.runLater(() -> {
                drawCommonLines(timelinePatternCanvas.getGraphicsContext2D());
            });
        });
    }

    private void drawCommonLines(GraphicsContext graphics) {
        drawLineAtPosition(graphics, trimStartPosition, Color.BLUE);
        drawLineAtPosition(graphics, trimEndPosition, Color.BLUE);
        drawLineAtPosition(graphics, trimmerTimelinePositionHolder.getCurrentPosition(), Color.YELLOW);
    }

    private void drawLineAtPosition(GraphicsContext graphics, TimelinePosition timelinePosition, Color color) {
        if (timelinePosition != null) {
            double position = timelinePosition.getSeconds().doubleValue() * pixelsPerSeconds;
            graphics.setStroke(color);
            graphics.strokeLine(position, 0, position, timelinePatternCanvas.getHeight());
        }
    }

    private void clearTimelineCanvas(Canvas timelineCanvas) {
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
        canvas.widthProperty().addListener((listener, oldValue, newValue) -> {
            showBottomScrollBarIfNeeded(bottomScroll, uiProjectRepository.getPreviewWidth());
        });
        canvas.heightProperty().addListener((listener, oldValue, newValue) -> {
            showBottomScrollBarIfNeeded(rightScroll, uiProjectRepository.getPreviewHeight());
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

    @Override
    public void resetDefaults() {
        trimmerTimelineManagerAccessor.removeAllClips();
        fileName = "";
        redrawTimelineCanvas();
        displayUpdaterService.updateCurrentPositionWithInvalidatedCache();
        timelineState.resetDefaults();
    }

}
