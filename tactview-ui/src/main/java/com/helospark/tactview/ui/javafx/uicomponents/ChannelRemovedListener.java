package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.ChannelRemovedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;

@Component
public class ChannelRemovedListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private NameToIdRepository nameToIdRepository;

    public ChannelRemovedListener(UiMessagingService messagingService, TimelineState timelineState, NameToIdRepository nameToIdRepository) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.nameToIdRepository = nameToIdRepository;
    }

    @PostConstruct
    public void setup() {
        messagingService.register(ChannelRemovedMessage.class, message -> onChannelRemoved(message));
    }

    private void onChannelRemoved(ChannelRemovedMessage message) {
        nameToIdRepository.removeId(message.getChannelId());
        timelineState.removeChannel(message.getChannelId());
    }

}
