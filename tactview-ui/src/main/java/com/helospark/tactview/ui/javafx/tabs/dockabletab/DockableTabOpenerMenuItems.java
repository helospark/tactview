package com.helospark.tactview.ui.javafx.tabs.dockabletab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.menu.DynamicallyGeneratedParentMenuContribution;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.message.DockableTabClosedMessage;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.message.DockableTabOpenedMessage;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.tab.DockableWindowMenuContribution;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

@Component
@Order(3999)
public class DockableTabOpenerMenuItems implements DynamicallyGeneratedParentMenuContribution {
    private List<DockableTabFactory> dockableTabFactories;
    private UiMessagingService uiMessagingService;
    private DockableTabRepository dockableTabRepository;
    private DockableTabFromIdFactory dockableTabFromIdFactory;

    private Map<String, BooleanProperty> selectedProperties = new HashMap<>();

    public DockableTabOpenerMenuItems(List<DockableTabFactory> dockableTabFactories, UiMessagingService uiMessagingService, DockableTabRepository dockableTabRepository,
            DockableTabFromIdFactory dockableTabFromIdFactory) {
        this.dockableTabFactories = dockableTabFactories;
        this.uiMessagingService = uiMessagingService;
        this.dockableTabRepository = dockableTabRepository;
        this.dockableTabFromIdFactory = dockableTabFromIdFactory;
    }

    @PostConstruct
    public void init() {
        uiMessagingService.register(DockableTabClosedMessage.class, message -> refreshSelectionModel());
        uiMessagingService.register(DockableTabOpenedMessage.class, message -> refreshSelectionModel());
    }

    private void refreshSelectionModel() {
        for (var entry : selectedProperties.entrySet()) {
            entry.getValue().set(dockableTabRepository.isTabOpen(entry.getKey()));
        }
    }

    @Override
    public List<String> getPath() {
        return List.of("Window", "View");
    }

    @Override
    public List<MenuContribution> getChildren() {
        return dockableTabFactories.stream()
                .map(a -> mapToContribution(a))
                .collect(Collectors.toList());
    }

    private MenuContribution mapToContribution(DockableTabFactory a) {
        String id = a.getId();
        BooleanProperty booleanProperty = new SimpleBooleanProperty();
        booleanProperty.set(dockableTabRepository.isTabOpen(id));
        selectedProperties.put(id, booleanProperty);
        return new DockableWindowMenuContribution(id, id, booleanProperty, dockableTabRepository, () -> dockableTabFromIdFactory.createTab(id));
    }

}
