package com.helospark.tactview.ui.javafx.clip;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.ui.javafx.clip.chain.ClipContextMenuChainItem;
import com.helospark.tactview.ui.javafx.clip.chain.ClipContextMenuChainItemRequest;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

@Component
public class ClipContextMenuFactory {
    private List<ClipContextMenuChainItem> chainItems;

    public ClipContextMenuFactory(List<ClipContextMenuChainItem> chainItems) {
        this.chainItems = chainItems;
    }

    public ContextMenu createContextMenuForClip(TimelineClip clip) {
        ClipContextMenuChainItemRequest request = new ClipContextMenuChainItemRequest(clip);
        List<MenuItem> items = chainItems.stream()
                .filter(item -> item.supports(request))
                .map(item -> item.createMenu(request))
                .collect(Collectors.toList());

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(items);

        return contextMenu;
    }

}
