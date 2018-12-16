package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.ClipRemovedMessage;
import com.helospark.tactview.core.timeline.message.EffectRemovedMessage;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;

@Component
public class ElementRemoveListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private NameToIdRepository nameToIdRepository;

    @Slf4j
    private Logger logger;

    public ElementRemoveListener(UiMessagingService messagingService, TimelineState timelineState, NameToIdRepository nameToIdRepository) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.nameToIdRepository = nameToIdRepository;
    }

    @PostConstruct
    public void setup() {
        messagingService.register(ClipRemovedMessage.class, message -> removeClip(message.getElementId()));
        messagingService.register(EffectRemovedMessage.class, message -> removeEffect(message.getEffectId()));
    }

    private void removeClip(String elementId) {
        nameToIdRepository.removeId(elementId);
        timelineState.removeClip(elementId);
        logger.debug("Clip {} successfuly removed", elementId);
    }

    private void removeEffect(String effectId) {
        nameToIdRepository.removeId(effectId);
        timelineState.removeEffect(effectId);
    }
}
