package com.helospark.tactview.ui.javafx.tabs.dockabletab;

import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;

public interface DockableTabFactory {

    public DetachableTab createTab();

    public boolean doesSupport(String id);

    public String getId();

}
