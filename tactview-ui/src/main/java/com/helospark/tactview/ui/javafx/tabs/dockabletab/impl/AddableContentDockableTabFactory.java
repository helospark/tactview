package com.helospark.tactview.ui.javafx.tabs.dockabletab.impl;

import java.util.Objects;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.tabs.TabActiveRequest;
import com.helospark.tactview.ui.javafx.tabs.TabFactory;
import com.helospark.tactview.ui.javafx.tabs.listener.TabCloseListener;
import com.helospark.tactview.ui.javafx.tabs.listener.TabOpenListener;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;

@Component
public class AddableContentDockableTabFactory extends AbstractCachingDockableTabFactory {
    public static final String ID = "addable-content";
    private LightDiContext lightDi;

    public AddableContentDockableTabFactory(LightDiContext lightDi) {
        this.lightDi = lightDi;
    }

    @Override
    public DetachableTab createTabInternal() {
        return new DetachableTab("Addable content", createEffectAdderTab(), ID);
    }

    private TabPane createEffectAdderTab() {
        TabPane tabPane = new TabPane();
        tabPane.setId("addable-content-tab-pane");
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        lightDi.getListOfBeans(TabFactory.class)
                .stream()
                .filter(a -> !a.isValueProvider())
                .forEach(tabFactory -> {
                    Tab tab = tabFactory.createTabContent();
                    tabPane.getTabs().add(tab);
                });
        lightDi.getBean(UiMessagingService.class).register(TabActiveRequest.class, message -> {
            tabPane.getTabs()
                    .stream()
                    .filter(tab -> Objects.equals(tab.getId(), message.getEditorId()))
                    .findFirst()
                    .ifPresent(foundTab -> tabPane.getSelectionModel().select(foundTab));
        });
        tabPane.getSelectionModel().selectedItemProperty()
                .addListener((e, oldValue, newValue) -> {
                    if (oldValue instanceof TabCloseListener) {
                        ((TabCloseListener) oldValue).tabClosed();
                    }
                    if (newValue instanceof TabOpenListener) {
                        ((TabOpenListener) newValue).tabOpened();
                    }
                });
        return tabPane;
    }

    @Override
    public boolean doesSupport(String id) {
        return ID.equals(id);
    }

    @Override
    public String getId() {
        return ID;
    }

}
