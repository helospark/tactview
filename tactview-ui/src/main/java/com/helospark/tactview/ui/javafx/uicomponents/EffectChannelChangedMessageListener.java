package com.helospark.tactview.ui.javafx.uicomponents;

import static com.helospark.tactview.ui.javafx.uicomponents.EffectAddedListener.EFFECTS_OFFSET;
import static com.helospark.tactview.ui.javafx.uicomponents.EffectAddedListener.EFFECT_HEIGHT;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.EffectChannelChangedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;

import javafx.scene.Node;

@Component
public class EffectChannelChangedMessageListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;

    public EffectChannelChangedMessageListener(UiMessagingService messagingService, TimelineState timelineState) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
    }

    @PostConstruct
    public void init() {
        messagingService.register(EffectChannelChangedMessage.class, message -> {
            String id = message.getId();
            Node effect = timelineState.findEffectById(id).get();
            effect.layoutYProperty().set(EFFECTS_OFFSET + EFFECT_HEIGHT * message.getNewChannelIndex());
        });
    }

}
