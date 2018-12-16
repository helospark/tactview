package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.ChannelAddedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@Component
public class ChannelAddedListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private TimelineDragAndDropHandler timelineDragAndDropHandler;
    private NameToIdRepository nameToIdRepository;

    public ChannelAddedListener(UiMessagingService messagingService, TimelineState timelineState, TimelineDragAndDropHandler timelineDragAndDropHandler, NameToIdRepository nameToIdRepository) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.timelineDragAndDropHandler = timelineDragAndDropHandler;
        this.nameToIdRepository = nameToIdRepository;
    }

    @PostConstruct
    public void setup() {
        messagingService.register(ChannelAddedMessage.class, message -> addChannel(message));
    }

    private void addChannel(ChannelAddedMessage message) {
        String generatedName = nameToIdRepository.generateAndAddNameForIdIfNotPresent("channel", message.getChannelId());

        System.out.println("Generated channel " + generatedName);

        HBox timeline = new HBox();
        timeline.setMinHeight(50);
        timeline.getStyleClass().add("timelinerow");
        timeline.setPrefWidth(2000);
        timeline.setMinWidth(2000);

        VBox timelineTitle = new VBox();
        timelineTitle.getChildren().add(new Label(generatedName));
        timelineTitle.setMaxWidth(200);
        timelineTitle.setMinWidth(150);
        timelineTitle.setPrefHeight(50);
        timelineTitle.getStyleClass().add("timeline-title");

        Pane timelineRow = new Pane();
        timelineRow.minWidth(2000);
        timelineRow.minHeight(50);
        timelineRow.getStyleClass().add("timeline-clips");
        timeline.getChildren().add(timelineRow);

        timelineTitle.prefHeightProperty().bind(timelineRow.heightProperty());

        timelineDragAndDropHandler.addDragAndDrop(timeline, timelineRow, message.getChannelId());

        timelineState.addChannel(message.getIndex(), message.getChannelId(), timeline, timelineTitle);
    }
}
