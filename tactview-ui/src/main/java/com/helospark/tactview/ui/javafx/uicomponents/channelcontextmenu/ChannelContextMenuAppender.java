package com.helospark.tactview.ui.javafx.uicomponents.channelcontextmenu;

import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand;
import com.helospark.tactview.ui.javafx.commands.impl.DuplicateChannelCommand;
import com.helospark.tactview.ui.javafx.commands.impl.MoveChannelCommand;
import com.helospark.tactview.ui.javafx.commands.impl.RemoveChannelCommand;
import com.helospark.tactview.ui.javafx.commands.impl.RemoveGapCommand;
import com.helospark.tactview.ui.javafx.repository.CopyPasteRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditModeRepository;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

@Component
public class ChannelContextMenuAppender {
    private TimelineManagerAccessor timelineManager;
    private UiCommandInterpreterService commandInterpreterService;
    private UiMessagingService messagingService;
    private TimelineEditModeRepository timelineEditModeRepository;
    private SelectedNodeRepository selectedNodeRepository;
    private CopyPasteRepository copyPasteRepository;

    public ChannelContextMenuAppender(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreterService, UiMessagingService messagingService,
            TimelineEditModeRepository timelineEditModeRepository, SelectedNodeRepository selectedNodeRepository, CopyPasteRepository copyPasteRepository) {
        this.timelineManager = timelineManager;
        this.commandInterpreterService = commandInterpreterService;
        this.messagingService = messagingService;
        this.timelineEditModeRepository = timelineEditModeRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.copyPasteRepository = copyPasteRepository;
    }

    public void addContextMenu(Node node, String channelId) {
        ContextMenu contextMenu = createContextMenu(channelId, Optional.empty());

        node.setOnContextMenuRequested(event -> {
            contextMenu.show(node.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    public ContextMenu createContextMenu(String channelId, Optional<TimelinePosition> position) {
        ContextMenu contextMenu = new ContextMenu();

        if (position.isPresent()) {
            int channelIndex = timelineManager.findChannelIndexByChannelId(channelId).orElse(0);
            addRemoveGapCommandMenuItemIfNeeded(position.get(), channelIndex, contextMenu);
        }

        contextMenu.getItems().add(createPasteMenuItem(channelId, position));
        contextMenu.getItems().add(new SeparatorMenuItem());

        contextMenu.getItems().add(createMoveChannelUpMenuItem(channelId));
        contextMenu.getItems().add(createMoveChannelDownMenuItem(channelId));

        contextMenu.getItems().add(new SeparatorMenuItem());

        contextMenu.getItems().add(addChannelAboveMenuItem(channelId));
        contextMenu.getItems().add(addChannelBelowMenuItem(channelId));
        contextMenu.getItems().add(duplicateChannelMenuItem(channelId));

        contextMenu.getItems().add(new SeparatorMenuItem());

        contextMenu.getItems().add(selectClipsInTabsMenuItem(channelId));

        contextMenu.getItems().add(new SeparatorMenuItem());

        contextMenu.getItems().add(deleteChannelMenuItem(channelId));

        return contextMenu;
    }

    private MenuItem createPasteMenuItem(String channelId, Optional<TimelinePosition> position) {
        MenuItem menuItem = new MenuItem("Paste");
        menuItem.setOnAction(e -> {
            if (copyPasteRepository.hasClipInClipboard()) {
                TimelinePosition actualPosition = position.orElse(timelineManager.findEndPosition(channelId).orElse(timelineManager.findEndPosition()));

                copyPasteRepository.pasteClipToPosition(channelId, actualPosition);
            }
        });
        if (!copyPasteRepository.hasClipInClipboard()) {
            menuItem.setDisable(true);
        }
        return menuItem;
    }

    private MenuItem selectClipsInTabsMenuItem(String channelId) {
        MenuItem menuItem = new MenuItem("Select clips on channel");
        menuItem.setOnAction(e -> {
            Optional<TimelineChannel> currentChannelIndex = timelineManager.findChannelWithId(channelId);
            if (currentChannelIndex.isPresent()) {
                selectedNodeRepository.clearAllSelectedItems();
                selectedNodeRepository.addSelectedClips(currentChannelIndex.get().getAllClipId());
            }
        });
        return menuItem;
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

    private void addRemoveGapCommandMenuItemIfNeeded(TimelinePosition position, int channelIndex, ContextMenu menu) {
        TreeSet<TimelineClip> clipsToRight = timelineManager.findClipsRightFromPositionAndOnChannelIgnoring(position, List.of(channelIndex), List.of());
        if (!clipsToRight.isEmpty()) {
            MenuItem removeGapMenuItem = new MenuItem("Remove gap");
            removeGapMenuItem.setOnAction(e -> {
                commandInterpreterService.sendWithResult(new RemoveGapCommand(timelineManager, messagingService, position, channelIndex, timelineEditModeRepository.getMode()));
            });
            menu.getItems().add(removeGapMenuItem);
        }
    }

}
