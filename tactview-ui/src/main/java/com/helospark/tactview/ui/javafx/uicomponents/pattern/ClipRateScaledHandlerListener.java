package com.helospark.tactview.ui.javafx.uicomponents.pattern;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.message.AbstractKeyframeChangedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;

import javafx.application.Platform;
import javafx.scene.shape.Rectangle;

@Component
public class ClipRateScaledHandlerListener {
    private MessagingService messagingService;
    private TimelineState timelineState;
    private TimelineManagerAccessor timelineManager;

    public ClipRateScaledHandlerListener(MessagingService messagingService, TimelineState timelineState, TimelineManagerAccessor timelineManager) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.timelineManager = timelineManager;
    }

    @PostConstruct
    public void init() {
        // TODO: custom message may be better?!
        messagingService.register(AbstractKeyframeChangedMessage.class, message -> {
            changeScaleIfNeeded(message.getContainingElementId());
        });
    }

    private void changeScaleIfNeeded(String clipId) {
        TimelineClip clip = timelineManager.findClipById(clipId).orElse(null);
        Rectangle clipRectangle = timelineState.findClipById(clipId).map(pane -> (Rectangle) pane.getChildren().get(0)).orElse(null);

        if (clip != null && clipRectangle != null) {
            TimelineLength newLength = clip.getInterval().getLength();
            double newWidth = timelineState.secondsToPixels(newLength);
            if (Math.abs(newWidth - clipRectangle.getWidth()) >= 0.01) {
                Platform.runLater(() -> clipRectangle.setWidth(newWidth));
            }
        }
    }

}
