package com.helospark.tactview.ui.javafx.uicomponents;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.message.ClipAddedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.TimelineImagePatternService;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

@Component
public class ClipAddedListener {
    private TimelineImagePatternService timelineImagePatternService;
    private MessagingService messagingService;
    private TimelineState timelineState;
    private EffectDragAdder effectDragAdder;
    private ProjectRepository projectRepository;
    private UiProjectRepository uiProjectRepository;
    private DragRepository dragRepository;
    private SelectedNodeRepository selectedNodeRepository;

    public ClipAddedListener(TimelineImagePatternService timelineImagePatternService, MessagingService messagingService, TimelineState timelineState, EffectDragAdder effectDragAdder, ProjectRepository projectRepository,
            UiProjectRepository uiProjectRepository, DragRepository dragRepository, SelectedNodeRepository selectedNodeRepository) {
        this.timelineImagePatternService = timelineImagePatternService;
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.effectDragAdder = effectDragAdder;
        this.projectRepository = projectRepository;
        this.uiProjectRepository = uiProjectRepository;
        this.dragRepository = dragRepository;
        this.selectedNodeRepository = selectedNodeRepository;
    }

    @PostConstruct
    public void setUp() {
        messagingService.register(ClipAddedMessage.class, message -> Platform.runLater(() -> addClip(message)));
    }

    private void addClip(ClipAddedMessage message) {
        TimelineClip clip = message.getClip();
        initializeProjectOnFirstVideoClipAdded(clip);

        timelineState.addClipForChannel(message.getChannelId(), message.getClipId(), createClip(message));
    }

    private void initializeProjectOnFirstVideoClipAdded(TimelineClip clip) {
        if (!projectRepository.isInitialized() && clip instanceof VisualTimelineClip) {
            VisualTimelineClip visualClip = (VisualTimelineClip) clip;
            projectRepository.initializer()
                    .withWidth(visualClip.getMediaMetadata().getWidth())
                    .withHeight(visualClip.getMediaMetadata().getHeight())
                    .withFps(visualClip instanceof VideoClip ? new BigDecimal(((VideoClip) visualClip).getMediaMetadata().getFps()) : new BigDecimal("30"))
                    .withIsInitialized(true)
                    .init();
            double horizontalScaleFactor = 320.0 / projectRepository.getWidth();
            double verticalScaleFactor = 260.0 / projectRepository.getHeight();
            double scale = Math.min(horizontalScaleFactor, verticalScaleFactor);
            double aspectRatio = ((double) visualClip.getMediaMetadata().getWidth()) / ((double) visualClip.getMediaMetadata().getHeight());
            int previewWidth = (int) (scale * visualClip.getMediaMetadata().getWidth());
            int previewHeight = (int) (scale * visualClip.getMediaMetadata().getHeight());
            uiProjectRepository.setScaleFactor(scale);
            uiProjectRepository.setPreviewWidth(previewWidth);
            uiProjectRepository.setPreviewHeight(previewHeight);
            uiProjectRepository.setAspectRatio(aspectRatio);
        }
    }

    public Group createClip(ClipAddedMessage clipAddedMessage) {
        TimelineClip clip = clipAddedMessage.getClip();
        Group parentPane = new Group();
        Rectangle rectangle = new Rectangle();
        int width = timelineState.secondsToPixels(clip.getInterval().getWidth());
        rectangle.setWidth(width);
        rectangle.setHeight(50);
        parentPane.translateXProperty().set(timelineState.secondsToPixels(clipAddedMessage.getPosition()));
        parentPane.setUserData(clipAddedMessage.getClipId());
        rectangle.getStyleClass().add("clip-rectangle");
        effectDragAdder.addEffectDragOnClip(parentPane, parentPane);

        if (clip instanceof VideoClip) {
            VideoClip videoClip = ((VideoClip) clip);
            timelineImagePatternService.createTimelinePattern(videoClip.getBackingSource().backingFile, videoClip.getMediaMetadata(), width)
                    .exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    })
                    .thenAccept(fillImage -> {
                        Platform.runLater(() -> rectangle.setFill(new ImagePattern(fillImage)));
                    });
        }
        parentPane.getStyleClass().add("timeline-clip");
        rectangle.setOnMouseClicked(event -> {
            selectedNodeRepository.setOnlySelectedClip(parentPane);
        });
        parentPane.getChildren().add(rectangle);

        rectangle.setOnDragDetected(e -> {
            Dragboard db = rectangle.startDragAndDrop(TransferMode.ANY);

            /* put a string on dragboard */
            ClipboardContent content = new ClipboardContent();

            timelineState.findClipById(clipAddedMessage.getClipId()).ifPresent(clip2 -> {
                TimelinePosition position = timelineState.pixelsToSeconds(clip2.getTranslateX());
                ClipDragInformation clipDragInformation = new ClipDragInformation(parentPane, position, clipAddedMessage.getClipId());
                dragRepository.onClipDragged(clipDragInformation);
                content.putString("moveclip");
            });

            db.setContent(content);
        });

        return parentPane;
    }

}
