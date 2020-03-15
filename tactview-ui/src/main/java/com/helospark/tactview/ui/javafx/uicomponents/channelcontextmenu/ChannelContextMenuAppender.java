package com.helospark.tactview.ui.javafx.uicomponents.channelcontextmenu;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand;
import com.helospark.tactview.ui.javafx.commands.impl.DuplicateChannelCommand;
import com.helospark.tactview.ui.javafx.commands.impl.MoveChannelCommand;
import com.helospark.tactview.ui.javafx.commands.impl.RemoveChannelCommand;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

@Component
public class ChannelContextMenuAppender {
    private TimelineManagerAccessor timelineManager;
    private UiCommandInterpreterService commandInterpreterService;

    public ChannelContextMenuAppender(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreterService) {
        this.timelineManager = timelineManager;
        this.commandInterpreterService = commandInterpreterService;
    }

    public void addContextMenu(Node node, String channelId) {
        ContextMenu contextMenu = new ContextMenu();

        contextMenu.getItems().add(createMoveChannelUpMenuItem(channelId));
        contextMenu.getItems().add(createMoveChannelDownMenuItem(channelId));

        contextMenu.getItems().add(new SeparatorMenuItem());

        contextMenu.getItems().add(addChannelAboveMenuItem(channelId));
        contextMenu.getItems().add(addChannelBelowMenuItem(channelId));
        contextMenu.getItems().add(duplicateChannelMenuItem(channelId));

        contextMenu.getItems().add(new SeparatorMenuItem());

        contextMenu.getItems().add(deleteChannelMenuItem(channelId));

        node.setOnContextMenuRequested(event -> {
            contextMenu.show(node.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    private MenuItem createMoveChannelUpMenuItem(String channelId) {
        MenuItem menuItem = new MenuItem("Move channel up");
        menuItem.setOnAction(e -> {
            Optional<Integer> currentChannelIndex = timelineManager.findChannelIndexByChannelId(channelId);
            if (currentChannelIndex.isPresent() && currentChannelIndex.get() != 0) {
                int index = currentChannelIndex.get();
                commandInterpreterService.sendWithResult(new MoveChannelCommand(timelineManager, index, index - 1));
            }
        });
        return menuItem;
    }

    private MenuItem createMoveChannelDownMenuItem(String channelId) {
        MenuItem menuItem = new MenuItem("Move channel down");
        menuItem.setOnAction(e -> {
            Optional<Integer> currentChannelIndex = timelineManager.findChannelIndexByChannelId(channelId);
            if (currentChannelIndex.isPresent() && currentChannelIndex.get() < timelineManager.getAllChannelIds().size() - 1) {
                int index = currentChannelIndex.get();
                commandInterpreterService.sendWithResult(new MoveChannelCommand(timelineManager, index, index + 1));
            }
        });
        return menuItem;
    }

    private MenuItem deleteChannelMenuItem(String channelId) {
        MenuItem menuItem = new MenuItem("Delete channel");
        menuItem.setOnAction(e -> commandInterpreterService.sendWithResult(new RemoveChannelCommand(timelineManager, channelId)));
        return menuItem;
    }

    private MenuItem duplicateChannelMenuItem(String channelId) {
        MenuItem menuItem = new MenuItem("Duplicate channel");
        menuItem.setOnAction(e -> commandInterpreterService.sendWithResult(new DuplicateChannelCommand(timelineManager, channelId)));
        return menuItem;
    }

    private MenuItem addChannelBelowMenuItem(String channelId) {
        MenuItem menuItem = new MenuItem("Create channel below");
        menuItem.setOnAction(e -> {
            Optional<Integer> currentChannelIndex = timelineManager.findChannelIndexByChannelId(channelId);
            if (currentChannelIndex.isPresent()) {
                int index = currentChannelIndex.get();
                commandInterpreterService.sendWithResult(new CreateChannelCommand(timelineManager, index + 1));
            }
        });
        return menuItem;
    }

    private MenuItem addChannelAboveMenuItem(String channelId) {
        MenuItem menuItem = new MenuItem("Create channel above");
        menuItem.setOnAction(e -> {
            Optional<Integer> currentChannelIndex = timelineManager.findChannelIndexByChannelId(channelId);
            if (currentChannelIndex.isPresent()) {
                int index = currentChannelIndex.get();
                commandInterpreterService.sendWithResult(new CreateChannelCommand(timelineManager, index));
            }
        });
        return menuItem;
    }

}
