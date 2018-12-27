package com.helospark.tactview.ui.javafx.effect;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.ui.javafx.effect.chain.EffectContextMenuChainItem;
import com.helospark.tactview.ui.javafx.effect.chain.EffectContextMenuChainItemRequest;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

@Component
public class EffectContextMenuFactory {
    private List<EffectContextMenuChainItem> chainItems;

    public EffectContextMenuFactory(List<EffectContextMenuChainItem> chainItems) {
        this.chainItems = chainItems;
    }

    public ContextMenu createContextMenuForEffect(StatelessEffect effect) {
        EffectContextMenuChainItemRequest request = new EffectContextMenuChainItemRequest(effect);
        List<MenuItem> items = chainItems.stream()
                .filter(item -> item.supports(request))
                .map(item -> item.createMenu(request))
                .collect(Collectors.toList());

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(items);

        return contextMenu;
    }

}
