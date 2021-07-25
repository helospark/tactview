package com.helospark.tactview.ui.javafx.tabs.dockabletab.tab;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.helospark.tactview.ui.javafx.menu.CheckboxMenuItemContribution;
import com.helospark.tactview.ui.javafx.menu.SelectableMenuContribution;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.DockableTabRepository;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.DockableTabRepository.OpenTabRequest;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.OpenDetachableTabTarget;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;

import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;

public class DockableWindowMenuContribution implements SelectableMenuContribution, CheckboxMenuItemContribution {
    private String path;
    private String id;
    private BooleanProperty booleanProperty;

    private Supplier<DetachableTab> tabFactory;
    private DockableTabRepository dockableTabRepository;
    private OpenDetachableTabTarget openDetachableTabTarget;
    private Optional<String> preferredNextTo;

    public DockableWindowMenuContribution(String path, String id, BooleanProperty booleanProperty, DockableTabRepository dockableTabRepository, Supplier<DetachableTab> tabFactory,
            OpenDetachableTabTarget openDetachableTabTarget, Optional<String> preferredNextTo) {
        this.path = path;
        this.id = id;
        this.booleanProperty = booleanProperty;
        this.dockableTabRepository = dockableTabRepository;
        this.tabFactory = tabFactory;
        this.openDetachableTabTarget = openDetachableTabTarget;
        this.preferredNextTo = preferredNextTo;
    }

    @Override
    public void onAction(ActionEvent event) {
        if (dockableTabRepository.isTabOpen(id)) {
            dockableTabRepository.closeTab(id);
        } else {
            OpenTabRequest openTabRequest = OpenTabRequest.builder()
                    .withTabToOpen(tabFactory.get())
                    .withTarget(openDetachableTabTarget)
                    .withSameTabPaneAs(preferredNextTo)
                    .build();

            dockableTabRepository.openTab(openTabRequest);
        }
    }

    @Override
    public BooleanProperty getSelectedProperty() {
        return booleanProperty;
    }

    @Override
    public List<String> getPath() {
        return List.of(path);
    }

}
