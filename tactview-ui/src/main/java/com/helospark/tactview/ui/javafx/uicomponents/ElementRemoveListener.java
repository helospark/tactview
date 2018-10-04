package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.ClipRemovedMessage;
import com.helospark.tactview.core.timeline.message.EffectRemovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

import javafx.application.Platform;

@Component
public class ElementRemoveListener {
    private MessagingService messagingService;
    private TimelineState timelineState;

    public ElementRemoveListener(MessagingService messagingService, TimelineState timelineState) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
    }

    @PostConstruct
    public void setup() {
        messagingService.register(ClipRemovedMessage.class, message -> Platform.runLater(() -> removeClip(message.getElementId())));
        messagingService.register(EffectRemovedMessage.class, message -> Platform.runLater(() -> removeEffect(message.getEffectId())));
    }

    private void removeClip(String elementId) {
        timelineState.removeClip(elementId);
    }

    private void removeEffect(String effectId) {
        timelineState.removeEffect(effectId);
    }
}
