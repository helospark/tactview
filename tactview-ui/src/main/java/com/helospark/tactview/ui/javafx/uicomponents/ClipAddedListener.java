package com.helospark.tactview.ui.javafx.uicomponents;

import java.math.BigDecimal;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.message.ClipAddedMessage;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.clip.ClipContextMenuFactory;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.projectsize.ProjectSizeInitializer;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;

import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

@Component
public class ClipAddedListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private EffectDragAdder effectDragAdder;
    private ProjectRepository projectRepository;
    private ProjectSizeInitializer projectSizeInitializer;
    private DragRepository dragRepository;
    private SelectedNodeRepository selectedNodeRepository;
    private NameToIdRepository nameToIdRepository;
    private ClipContextMenuFactory clipContextMenuFactory;

    @Slf4j
    private Logger logger;

    public ClipAddedListener(UiMessagingService messagingService, TimelineState timelineState, EffectDragAdder effectDragAdder,
            ProjectRepository projectRepository,
            ProjectSizeInitializer projectSizeInitializer, DragRepository dragRepository, SelectedNodeRepository selectedNodeRepository,
            NameToIdRepository nameToIdRepository, ClipContextMenuFactory clipContextMenuAdder) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.effectDragAdder = effectDragAdder;
        this.projectRepository = projectRepository;
        this.projectSizeInitializer = projectSizeInitializer;
        this.dragRepository = dragRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.nameToIdRepository = nameToIdRepository;
        this.clipContextMenuFactory = clipContextMenuAdder;
    }

    @PostConstruct
    public void setUp() {
        messagingService.register(ClipAddedMessage.class, message -> addClip(message));
    }

    private void addClip(ClipAddedMessage message) {
        TimelineClip clip = message.getClip();
        initializeProjectOnFirstVideoClipAdded(clip);

        timelineState.addClipForChannel(message.getChannelId(), message.getClipId(), createClip(message));
        logger.debug("Clip {} added successfuly", message.getClipId());
    }

    private void initializeProjectOnFirstVideoClipAdded(TimelineClip clip) {
        if (!projectRepository.isVideoInitialized() && clip instanceof VisualTimelineClip) {
            VisualTimelineClip visualClip = (VisualTimelineClip) clip;
            VisualMediaMetadata metadata = visualClip.getMediaMetadata();
            int width = visualClip.getMediaMetadata().getWidth();
            int height = visualClip.getMediaMetadata().getHeight();
            BigDecimal fps = metadata instanceof VideoMetadata ? new BigDecimal(((VideoMetadata) metadata).getFps()) : new BigDecimal("30");

            projectSizeInitializer.initializeProjectSize(width, height, fps);
        }
        if (!projectRepository.isAudioInitialized() && clip instanceof AudibleTimelineClip) {
            AudibleTimelineClip audioClip = (AudibleTimelineClip) clip;
            int sampleRate = audioClip.getMediaMetadata().getSampleRate();
            int bytesPerSample = audioClip.getMediaMetadata().getBytesPerSample();
            int numberOfChannels = audioClip.getMediaMetadata().getChannels();
            projectRepository.initializeAudio(sampleRate, bytesPerSample, numberOfChannels);
        }
    }

    public Pane createClip(ClipAddedMessage clipAddedMessage) {
        TimelineClip clip = clipAddedMessage.getClip();
        nameToIdRepository.generateAndAddNameForIdIfNotPresent(clip.getClass().getSimpleName(), clip.getId());
        Pane parentPane = new Pane();
        Rectangle rectangle = new Rectangle();
        int width = timelineState.secondsToPixels(clip.getInterval().getLength());
        rectangle.setWidth(width);
        rectangle.setHeight(50);
        parentPane.layoutXProperty().set(timelineState.secondsToPixels(clipAddedMessage.getPosition()));
        parentPane.setUserData(clipAddedMessage.getClipId());
        rectangle.getStyleClass().add("clip-rectangle");
        effectDragAdder.addEffectDragOnClip(parentPane, clip.getId());

        parentPane.getStyleClass().add("timeline-clip");
        rectangle.setOnMouseClicked(event -> {
            if (!selectedNodeRepository.getSelectedClipIds().isEmpty() && !event.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }
            if (event.isControlDown()) {
                selectedNodeRepository.addSelectedClip(parentPane);
            } else {
                selectedNodeRepository.setOnlySelectedClip(parentPane);
            }
        });
        parentPane.getChildren().add(rectangle);

        rectangle.setOnDragDetected(e -> {
            Dragboard db = rectangle.startDragAndDrop(TransferMode.ANY);
            double currentX = e.getX();

            /* put a string on dragboard */
            ClipboardContent content = new ClipboardContent();

            timelineState.findClipById(clipAddedMessage.getClipId()).ifPresent(clip2 -> {
                double xPosition = clip2.getLayoutX();
                TimelinePosition position = timelineState.pixelsToSeconds(xPosition);
                String channelId = (String) timelineState.findChannelForClip(clip2).get().getUserData();
                ClipDragInformation clipDragInformation = new ClipDragInformation(parentPane, position, clipAddedMessage.getClipId(), channelId, currentX);
                if (isResizing(clipAddedMessage, rectangle, currentX)) {
                    DragRepository.DragDirection direction = isDraggingLeft(rectangle, currentX) ? DragRepository.DragDirection.LEFT : DragRepository.DragDirection.RIGHT;
                    dragRepository.onClipResizing(clipDragInformation, direction);
                } else {
                    dragRepository.onClipDragged(clipDragInformation);
                }
                content.putString("moveclip");
            });

            db.setContent(content);
        });

        rectangle.setOnMouseMoved(event -> {
            double currentX = event.getX();
            if (isResizing(clipAddedMessage, rectangle, currentX)) {
                rectangle.setCursor(Cursor.H_RESIZE);
            } else {
                rectangle.setCursor(Cursor.HAND);
            }
        });

        rectangle.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            Optional<ContextMenu> contextMenu = clipContextMenuFactory.createContextMenuForSelectedClips();
            if (contextMenu.isPresent()) {
                contextMenu.get().show(rectangle.getScene().getWindow(), event.getScreenX(), event.getScreenY());
                event.consume();
            }
        });

        return parentPane;
    }

    private boolean isResizing(ClipAddedMessage clipAddedMessage, Rectangle rectangle, double currentX) {
        return (isDraggingLeft(rectangle, currentX) ||
                isDraggingRight(rectangle, currentX)) &&
                clipAddedMessage.isResizable();
    }

    private boolean isDraggingLeft(Rectangle rectangle, double currentX) {
        return currentX - rectangle.getLayoutX() < 15 / timelineState.getZoom();
    }

    private boolean isDraggingRight(Rectangle rectangle, double currentX) {
        return rectangle.getLayoutX() + rectangle.getWidth() - currentX < 15 / timelineState.getZoom();
    }

}
