package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.message.ChannelAddedMessage;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.commands.impl.DisableChannelCommand;
import com.helospark.tactview.ui.javafx.commands.impl.MoveChannelCommand;
import com.helospark.tactview.ui.javafx.commands.impl.MuteChannelCommand;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.uicomponents.channelcontextmenu.ChannelContextMenuAppender;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Component
public class ChannelAddedListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelAddedListener.class);
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private NameToIdRepository nameToIdRepository;
    private UiCommandInterpreterService commandInterpreterService;
    private TimelineManagerAccessor timelineManager;
    private ChannelContextMenuAppender channelContextMenuAppender;

    public ChannelAddedListener(UiMessagingService messagingService, TimelineState timelineState, NameToIdRepository nameToIdRepository,
            UiCommandInterpreterService commandInterpreterService, TimelineManagerAccessor timelineManager, ChannelContextMenuAppender channelContextMenuAppender) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.nameToIdRepository = nameToIdRepository;
        this.commandInterpreterService = commandInterpreterService;
        this.timelineManager = timelineManager;
        this.channelContextMenuAppender = channelContextMenuAppender;
    }

    @PostConstruct
    public void setup() {
        messagingService.register(ChannelAddedMessage.class, message -> addChannel(message));
    }

    private void addChannel(ChannelAddedMessage message) {
        String generatedName = nameToIdRepository.generateAndAddNameForIdIfNotPresent("channel", message.getChannelId());

        LOGGER.debug("Generated channel " + generatedName);

        VBox timelineTitle = new VBox();
        TextField timelineTitleChannelNameLabel = new TextField(generatedName);
        timelineTitleChannelNameLabel.getStyleClass().add("timeline-title-channel-name-label");
        timelineTitleChannelNameLabel.setTooltip(new Tooltip("Change channel name"));

        timelineTitleChannelNameLabel.setOnKeyReleased(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                nameToIdRepository.addNameForId(e.getText(), message.getChannelId());
            }
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                timelineTitleChannelNameLabel.setText(nameToIdRepository.getNameForId(message.getChannelId()));
            }
        });
        timelineTitleChannelNameLabel.focusedProperty().addListener((value, oldValue, newValue) -> {
            if (oldValue == true && newValue == false) {
                nameToIdRepository.addNameForId(timelineTitleChannelNameLabel.getText(), message.getChannelId());
            }
        });

        timelineTitle.getChildren().add(timelineTitleChannelNameLabel);

        HBox buttonBar = new HBox();
        buttonBar.getStyleClass().add("channel-header-button-bar");

        ToggleButton disableButton = new ToggleButton("", new Glyph("FontAwesome", FontAwesome.Glyph.EYE_SLASH));
        disableButton.getStyleClass().add("channel-title-button");
        disableButton.setTooltip(new Tooltip("Disable channel"));
        disableButton.setSelected(message.isDisabled());
        disableButton.setOnAction(e -> {
            boolean isDisable = disableButton.isSelected();
            commandInterpreterService.sendWithResult(new DisableChannelCommand(timelineManager, message.getChannelId(), isDisable));
        });
        buttonBar.getChildren().add(disableButton);

        ToggleButton muteButton = new ToggleButton("", new Glyph("FontAwesome", FontAwesome.Glyph.VOLUME_OFF));
        muteButton.getStyleClass().add("channel-title-button");
        muteButton.setTooltip(new Tooltip("Mute channel audio"));
        muteButton.setSelected(message.isMute());
        muteButton.setOnAction(e -> {
            boolean isMute = muteButton.isSelected();
            commandInterpreterService.sendWithResult(new MuteChannelCommand(timelineManager, message.getChannelId(), isMute));
        });
        buttonBar.getChildren().add(muteButton);

        Button moveChannelUpButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_UP));
        moveChannelUpButton.getStyleClass().add("channel-title-button");
        moveChannelUpButton.setTooltip(new Tooltip("Move channel up"));
        moveChannelUpButton.setOnAction(e -> {
            Optional<Integer> currentChannelIndex = timelineManager.findChannelIndexByChannelId(message.getChannelId());
            if (currentChannelIndex.isPresent() && currentChannelIndex.get() != 0) {
                int index = currentChannelIndex.get();
                commandInterpreterService.sendWithResult(new MoveChannelCommand(timelineManager, index, index - 1));
            }
        });
        buttonBar.getChildren().add(moveChannelUpButton);

        Button moveChannelDownButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_DOWN));
        moveChannelDownButton.getStyleClass().add("channel-title-button");
        moveChannelDownButton.setTooltip(new Tooltip("Move channel down"));
        moveChannelDownButton.setOnAction(e -> {
            Optional<Integer> currentChannelIndex = timelineManager.findChannelIndexByChannelId(message.getChannelId());
            if (currentChannelIndex.isPresent() && currentChannelIndex.get() < timelineManager.getAllChannelIds().size() - 1) {
                int index = currentChannelIndex.get();
                commandInterpreterService.sendWithResult(new MoveChannelCommand(timelineManager, index, index + 1));
            }
        });
        buttonBar.getChildren().add(moveChannelDownButton);

        timelineTitle.getChildren().add(buttonBar);

        timelineTitle.setMaxWidth(200);
        timelineTitle.setMinWidth(150);
        timelineTitle.getStyleClass().add("timeline-title");

        channelContextMenuAppender.addContextMenu(timelineTitle, message.getChannelId());

        timelineState.addChannelHeader(message.getChannelId(), timelineTitle, message.getIndex());
    }
}
