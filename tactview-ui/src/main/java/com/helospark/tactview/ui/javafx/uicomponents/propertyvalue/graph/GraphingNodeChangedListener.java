package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.GraphNodeAddedMessage;
import com.helospark.tactview.core.timeline.message.GraphNodeRemovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;

@Component
public class GraphingNodeChangedListener {
    private MessagingService messagingService;
    private NameToIdRepository nameToIdRepository;

    public GraphingNodeChangedListener(MessagingService messagingService, NameToIdRepository nameToIdRepository) {
        this.messagingService = messagingService;
        this.nameToIdRepository = nameToIdRepository;
    }

    @PostConstruct
    public void init() {
        messagingService.register(GraphNodeAddedMessage.class, message -> {
            nameToIdRepository.generateAndAddNameForIdIfNotPresent(message.getGraphElement().getName(), message.getGraphElement().getId());
        });
        messagingService.register(GraphNodeRemovedMessage.class, message -> {
            nameToIdRepository.removeId(message.getGraphElement().getId());
        });
    }

}
