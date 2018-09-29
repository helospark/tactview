package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.core.timeline.message.ClipAddedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.TimelineImagePatternService;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

@Component
public class ClipAddedListener {
    private TimelineImagePatternService timelineImagePatternService;
    private MessagingService messagingService;
    private TimelineState timelineState;
    private EffectDragAdder effectDragAdder;

    public ClipAddedListener(TimelineImagePatternService timelineImagePatternService, MessagingService messagingService, TimelineState timelineState, EffectDragAdder effectDragAdder) {
        this.timelineImagePatternService = timelineImagePatternService;
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.effectDragAdder = effectDragAdder;
    }

    @PostConstruct
    public void setUp() {
        messagingService.register(ClipAddedMessage.class, message -> Platform.runLater(() -> addClip(message)));
    }

    private void addClip(ClipAddedMessage message) {
        //        Optional<Pane> channel = timelineState.findChannelById(message.getChannelId());
        //        if (!channel.isPresent()) {
        //            // create channel
        //        }
        //        Pane actualChannel = channel.get();
        timelineState.addClipForChannel(message.getChannelId(), createClip(message));
    }

    public Group createClip(ClipAddedMessage clipAddedMessage) {
        TimelineClip clip = clipAddedMessage.getClip();
        Group parentPane = new Group();
        Rectangle rectangle = new Rectangle();
        int width = timelineState.secondsToPixels(clip.getInterval().getWidth());
        rectangle.setWidth(width);
        rectangle.setHeight(50);
        parentPane.translateXProperty().set(timelineState.secondsToPixels(clipAddedMessage.getPosition()));
        //        idToNode.put(addedClipId, () -> timelineRow.getChildren().remove(droppedElement));
        parentPane.setUserData(clipAddedMessage.getClipId());
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
        parentPane.getChildren().add(rectangle);
        return parentPane;
    }

}
