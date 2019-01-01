package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.PropertyValueContextMenuItem;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.PropertyValueContextMenuRequest;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;

@Component
public class PropertyValueSetterChain {
    private List<PropertyValueSetterChainItem> items;
    private List<PropertyValueContextMenuItem> contextMenuList;
    private UiTimelineManager timelineManager;

    public PropertyValueSetterChain(List<PropertyValueSetterChainItem> items, List<PropertyValueContextMenuItem> contextMenuList, UiTimelineManager timelineManager) {
        this.items = items;
        this.contextMenuList = contextMenuList;
        this.timelineManager = timelineManager;
    }

    public EffectLine create(ValueProviderDescriptor descriptor) {
        EffectLine result = createEffectLine(descriptor);

        addContextMenuIfRequired(result, descriptor);

        return result;
    }

    private EffectLine createEffectLine(ValueProviderDescriptor descriptor) {
        return items.stream()
                .filter(e -> e.doesSupport(descriptor.getKeyframeableEffect()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No chain item for " + descriptor))
                .create(descriptor, descriptor.getKeyframeableEffect());
    }

    private void addContextMenuIfRequired(EffectLine result, ValueProviderDescriptor descriptor) {
        ContextMenu contextMenu = new ContextMenu();

        result.visibleNode.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
            PropertyValueContextMenuRequest request = new PropertyValueContextMenuRequest(descriptor.getKeyframeableEffect(), descriptor, result, timelineManager.getCurrentPosition());
            List<MenuItem> contextMenuItems = contextMenuList.stream()
                    .filter(listItem -> listItem.supports(request))
                    .map(listItem -> listItem.createMenuItem(request))
                    .collect(Collectors.toList());

            System.out.println(contextMenuItems);

            if (contextMenuList.size() > 0) {
                contextMenu.getItems().clear();
                contextMenu.getItems().addAll(contextMenuItems);
                contextMenu.show(result.visibleNode.getScene().getWindow(), e.getScreenX(), e.getScreenY());
                e.consume();
            }
        });
    }
}
