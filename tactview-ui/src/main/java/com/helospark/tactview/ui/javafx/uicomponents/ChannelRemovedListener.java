package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.ChannelRemovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;

import javafx.application.Platform;

@Component
public class ChannelRemovedListener {
    private MessagingService messagingService;
    private TimelineState timelineState;
    private NameToIdRepository nameToIdRepository;

    public ChannelRemovedListener(MessagingService messagingService, TimelineState timelineState, NameToIdRepository nameToIdRepository) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.nameToIdRepository = nameToIdRepository;
    }

    @PostConstruct
    public void setup() {
        messagingService.register(ChannelRemovedMessage.class, message -> Platform.runLater(() -> onChannelRemoved(message)));
    }

    private void onChannelRemoved(ChannelRemovedMessage message) {
        nameToIdRepository.removeId(message.getChannelId());
        timelineState.removeChannel(message.getChannelId());
    }

}
