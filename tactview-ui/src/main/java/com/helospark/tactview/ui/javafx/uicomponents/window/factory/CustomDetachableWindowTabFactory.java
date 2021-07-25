package com.helospark.tactview.ui.javafx.uicomponents.window.factory;

import com.helospark.tactview.ui.javafx.tabs.dockabletab.DockableTabFactory;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.OpenDetachableTabTarget;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;

public class CustomDetachableWindowTabFactory implements DockableTabFactory {
    private DetachableTab window;

    public CustomDetachableWindowTabFactory(DetachableTab window) {
        this.window = window;
    }

    @Override
    public DetachableTab createTab() {
        return window;
    }

    @Override
    public boolean doesSupport(String id) {
        return window.getId().equals(id);
    }

    @Override
    public String getId() {
        return window.getId();
    }

    @Override
    public OpenDetachableTabTarget getPreferredMode() {
        return OpenDetachableTabTarget.NEW_WINDOW;
    }

}
