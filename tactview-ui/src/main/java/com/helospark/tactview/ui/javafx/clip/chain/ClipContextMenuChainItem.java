package com.helospark.tactview.ui.javafx.clip.chain;

import javafx.scene.control.MenuItem;

public interface ClipContextMenuChainItem {

    public MenuItem createMenu(ClipContextMenuChainItemRequest request);

    public boolean supports(ClipContextMenuChainItemRequest request);

}
