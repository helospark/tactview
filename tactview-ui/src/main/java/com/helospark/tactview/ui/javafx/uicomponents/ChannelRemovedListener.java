package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.message.ChannelRemovedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;

@Component
public class ChannelRemovedListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private NameToIdRepository nameToIdRepository;
    private TimelineManagerAccessor timelineAccessor;

    public ChannelRemovedListener(UiMessagingService messagingService, TimelineState timelineState, NameToIdRepository nameToIdRepository, TimelineManagerAccessor timelineAccessor) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.nameToIdRepository = nameToIdRepository;
        this.timelineAccessor = timelineAccessor;
    }

    @PostConstruct
    public void setup() {
        messagingService.register(ChannelRemovedMessage.class, message -> onChannelRemoved(message));
    }

    private void onChannelRemoved(ChannelRemovedMessage message) {
        nameToIdRepository.removeId(message.getChannelId());
        timelineState.removeChannel(timelineAccessor.findChannelIndexByChannelId(message.getChannelId()));
    }

}
