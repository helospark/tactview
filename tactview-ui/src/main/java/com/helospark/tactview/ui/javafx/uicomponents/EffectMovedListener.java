package com.helospark.tactview.ui.javafx.uicomponents;

import static com.helospark.tactview.ui.javafx.uicomponents.EffectAddedListener.EFFECTS_OFFSET;
import static com.helospark.tactview.ui.javafx.uicomponents.EffectAddedListener.EFFECT_HEIGHT;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.message.EffectMovedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.util.SpecialPointLineDrawer;

@Component
public class EffectMovedListener {
    private TimelineState timelineState;
    private UiMessagingService messagingService;
    private SpecialPointLineDrawer specialPointLineDrawer;

    public EffectMovedListener(TimelineState timelineState, UiMessagingService messagingService, SpecialPointLineDrawer specialPointLineDrawer) {
        this.timelineState = timelineState;
        this.messagingService = messagingService;
        this.specialPointLineDrawer = specialPointLineDrawer;
    }

    @PostConstruct
    public void init() {
        this.messagingService.register(EffectMovedMessage.class, message -> {
            double position = timelineState.secondsToPixels(message.getNewPosition());
            timelineState.findEffectById(message.getEffectId())
                    .ifPresent(effect -> {
                        System.out.println("Moved to " + position);
                        effect.setLayoutX(position);
                        effect.setLayoutY(EFFECTS_OFFSET + EFFECT_HEIGHT * message.getNewChannelIndex());
                        if (message.getSpecialPositionUsed().isPresent() && message.isMoreMoveExpected()) {
                            drawSpecialPositionLine(message);
                        } else {
                            timelineState.disableSpecialPointLineProperties();
                        }
                    });
        });
    }

    private void drawSpecialPositionLine(EffectMovedMessage message) {
        ClosesIntervalChannel specialPosition = message.getSpecialPositionUsed().get();
        String originalClipId = message.getOriginalClipId();
        specialPointLineDrawer.drawSpecialPointLineForEffect(specialPosition, originalClipId);
    }

}
