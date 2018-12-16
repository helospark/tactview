package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.Optional;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.messaging.EffectMovedToDifferentClipMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;

import javafx.scene.Node;

@Component
public class EffectMovedToDifferentClipMessageListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;

    public EffectMovedToDifferentClipMessageListener(UiMessagingService messagingService, TimelineState timelineState) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
    }

    @PostConstruct
    public void init() {
        messagingService.register(EffectMovedToDifferentClipMessage.class, message -> {
            Optional<Node> removedEffect = timelineState.removeEffect(message.getEffectId());
            removedEffect.ifPresent(node -> {
                timelineState.addEffectToClip(message.getNewClipId(), node);
            });
        });
    }
}
