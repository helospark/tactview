package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.EffectLine;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;

@Component
public class ContextMenuAppender {
    private List<PropertyValueContextMenuItem> contextMenuList;
    private GlobalTimelinePositionHolder timelineManager;

    public ContextMenuAppender(List<PropertyValueContextMenuItem> contextMenuList, GlobalTimelinePositionHolder timelineManager) {
        this.contextMenuList = contextMenuList;
        this.timelineManager = timelineManager;
    }

    public void addContextMenu(EffectLine result, KeyframeableEffect effect, ValueProviderDescriptor descriptor, Node node) {
        ContextMenu contextMenu = new ContextMenu();

        node.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
            PropertyValueContextMenuRequest request = new PropertyValueContextMenuRequest(effect, descriptor, result, timelineManager.getCurrentPosition());
            List<MenuItem> contextMenuItems = contextMenuList.stream()
                    .filter(listItem -> listItem.supports(request))
                    .map(listItem -> listItem.createMenuItem(request))
                    .collect(Collectors.toList());

            if (contextMenuList.size() > 0) {
                contextMenu.getItems().clear();
                contextMenu.getItems().addAll(contextMenuItems);
                contextMenu.show(node.getScene().getWindow(), e.getScreenX(), e.getScreenY());
                e.consume();
            }
        });
    }

}
