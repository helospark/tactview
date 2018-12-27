package com.helospark.tactview.ui.javafx.effect.chain;

import javafx.scene.control.MenuItem;

public interface EffectContextMenuChainItem {

    public MenuItem createMenu(EffectContextMenuChainItemRequest request);

    public boolean supports(EffectContextMenuChainItemRequest request);

}
