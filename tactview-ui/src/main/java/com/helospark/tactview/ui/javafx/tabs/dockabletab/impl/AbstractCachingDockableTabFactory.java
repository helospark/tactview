package com.helospark.tactview.ui.javafx.tabs.dockabletab.impl;

import com.helospark.tactview.ui.javafx.tabs.dockabletab.DockableTabFactory;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;

public abstract class AbstractCachingDockableTabFactory implements DockableTabFactory {
    private DetachableTab cachedTab;

    @Override
    public DetachableTab createTab() {
        if (cachedTab == null) {
            cachedTab = createTabInternal();
            return cachedTab;
        } else {
            return cachedTab;
        }
    }

    protected abstract DetachableTab createTabInternal();

}
