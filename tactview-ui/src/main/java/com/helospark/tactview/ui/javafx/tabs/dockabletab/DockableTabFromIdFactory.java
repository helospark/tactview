package com.helospark.tactview.ui.javafx.tabs.dockabletab;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;

@Component
public class DockableTabFromIdFactory {
    private List<DockableTabFactory> dockableTabFactories;

    public DockableTabFromIdFactory(List<DockableTabFactory> dockableTabFactories) {
        this.dockableTabFactories = dockableTabFactories;
    }

    public DetachableTab createTab(String id) {
        return dockableTabFactories.stream()
                .filter(factory -> factory.doesSupport(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot create tab " + id))
                .createTab();
    }
}
