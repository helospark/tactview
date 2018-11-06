package com.helospark.tactview.ui.javafx.uicomponents;

import static com.helospark.tactview.ui.javafx.uicomponents.EffectAddedListener.EFFECTS_OFFSET;
import static com.helospark.tactview.ui.javafx.uicomponents.EffectAddedListener.EFFECT_HEIGHT;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.message.EffectMovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

import javafx.application.Platform;
import javafx.scene.layout.HBox;

@Component
public class EffectMovedListener {
    private TimelineState timelineState;
    private MessagingService messagingService;

    public EffectMovedListener(TimelineState timelineState, MessagingService messagingService) {
        this.timelineState = timelineState;
        this.messagingService = messagingService;
    }

    @PostConstruct
    public void init() {
        this.messagingService.register(EffectMovedMessage.class, message -> Platform.runLater(() -> {
            int position = timelineState.secondsToPixels(message.getNewPosition());
            timelineState.findEffectById(message.getEffectId())
                    .ifPresent(effect -> {
                        effect.setLayoutX(position);
                        effect.setLayoutY(EFFECTS_OFFSET + EFFECT_HEIGHT * message.getNewChannelIndex());
                        if (message.getSpecialPositionUsed().isPresent()) {
                            drawSpecialPositionLine(message);
                        } else {
                            timelineState.getMoveSpecialPointLineProperties().setEnabledProperty(false);
                        }
                    });
        }));
    }

    private void drawSpecialPositionLine(EffectMovedMessage message) {
        ClosesIntervalChannel specialPosition = message.getSpecialPositionUsed().get();
        HBox specialPositionChannel = timelineState.findChannelById(specialPosition.getChannelId()).orElseThrow();
        HBox currentChannel = timelineState.findChannelForClip(message.getOriginalClipId()).orElseThrow();

        int lineStartX = timelineState.secondsToPixels(specialPosition.getSpecialPosition());
        int lineEndX = timelineState.secondsToPixels(specialPosition.getSpecialPosition());

        int lineStartY = (int) specialPositionChannel.getLayoutY();
        int lineEndY = (int) (currentChannel.getLayoutY() + currentChannel.getHeight());

        MoveSpecialPointLineProperties properties = timelineState.getMoveSpecialPointLineProperties();
        properties.setStartX(lineStartX);
        properties.setStartY(lineStartY);
        properties.setEndX(lineEndX);
        properties.setEndY(lineEndY);
        properties.setEnabledProperty(true);
    }

}
