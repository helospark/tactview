package com.helospark.tactview.ui.javafx.clip.chain;

import java.math.BigDecimal;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiTimelineManager;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

@Component
@Order(93)
public class CursorMoveToContextMenuChainItem implements ClipContextMenuChainItem {
    private TimelineManagerAccessor timelineManagerAccessor;
    private UiTimelineManager uiTimelineManager;

    public CursorMoveToContextMenuChainItem(TimelineManagerAccessor timelineManagerAccessor, UiTimelineManager uiTimelineManager) {
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.uiTimelineManager = uiTimelineManager;
    }

    @Override
    public MenuItem createMenu(ClipContextMenuChainItemRequest request) {
        Menu menu = new Menu("Cursor to");

        menu.getItems().add(createMoveToLeftMenuItem(request));
        menu.getItems().add(createMoveToRightMenuItem(request));
        menu.getItems().add(createEndOfChannelMenuItem(request));
        menu.getItems().add(createStartOfChannelMenuItem(request));

        return menu;
    }

    private MenuItem createMoveToRightMenuItem(ClipContextMenuChainItemRequest request) {
        MenuItem moveToRightMenuItem = new MenuItem("right of clip");

        moveToRightMenuItem.setOnAction(e -> {
            TimelinePosition endPosition = request.getPrimaryClip().getInterval().getEndPosition();
            uiTimelineManager.jumpAbsolute(endPosition.getSeconds());
        });

        return moveToRightMenuItem;
    }

    private MenuItem createMoveToLeftMenuItem(ClipContextMenuChainItemRequest request) {
        MenuItem moveToRightMenuItem = new MenuItem("left of clip");

        moveToRightMenuItem.setOnAction(e -> {
            TimelinePosition endPosition = request.getPrimaryClip().getInterval().getStartPosition();
            uiTimelineManager.jumpAbsolute(endPosition.getSeconds());
        });

        return moveToRightMenuItem;
    }

    private MenuItem createEndOfChannelMenuItem(ClipContextMenuChainItemRequest request) {
        MenuItem moveToRightMenuItem = new MenuItem("end of channel");

        moveToRightMenuItem.setOnAction(e -> {
            String channelId = timelineManagerAccessor.findChannelForClipId(request.getPrimaryClip().getId()).get().getId();
            TimelinePosition endPosition = timelineManagerAccessor.findEndPosition(channelId).get();
            uiTimelineManager.jumpAbsolute(endPosition.getSeconds());
        });

        return moveToRightMenuItem;
    }

    private MenuItem createStartOfChannelMenuItem(ClipContextMenuChainItemRequest request) {
        MenuItem moveToRightMenuItem = new MenuItem("start of channel");

        moveToRightMenuItem.setOnAction(e -> {
            uiTimelineManager.jumpAbsolute(BigDecimal.ZERO);
        });

        return moveToRightMenuItem;
    }

    @Override
    public boolean supports(ClipContextMenuChainItemRequest request) {
        return true;
    }

}
