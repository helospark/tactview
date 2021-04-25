package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;

@Component
public class EffectAddedListener {
    private UiMessagingService messagingService;
    private NameToIdRepository nameToIdRepository;

    public EffectAddedListener(UiMessagingService messagingService, NameToIdRepository nameToIdRepository) {
        this.messagingService = messagingService;
        this.nameToIdRepository = nameToIdRepository;
    }

    @PostConstruct
    public void setUp() {
        messagingService.register(EffectAddedMessage.class, message -> addEffectClip(message));
    }

    private void addEffectClip(EffectAddedMessage effectAddedMessage) {
        nameToIdRepository.generateAndAddNameForIdIfNotPresent(effectAddedMessage.getEffect().getClass().getSimpleName(), effectAddedMessage.getEffectId());
    }

}
