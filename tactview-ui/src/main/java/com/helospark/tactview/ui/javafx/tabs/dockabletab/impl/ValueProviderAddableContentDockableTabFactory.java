package com.helospark.tactview.ui.javafx.tabs.dockabletab.impl;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.tabs.TabFactory;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;

@Component
public class ValueProviderAddableContentDockableTabFactory extends AbstractCachingDockableTabFactory {
    public static final String ID = "value-provider-addable-content";
    private LightDiContext lightDi;

    public ValueProviderAddableContentDockableTabFactory(LightDiContext lightDi) {
        this.lightDi = lightDi;
    }

    @Override
    public DetachableTab createTabInternal() {
        return new DetachableTab("Value providers", createEffectAdderTab(), ID);
    }

    private TabPane createEffectAdderTab() {
        TabPane tabPane = new TabPane();
        tabPane.setId("addable-content-tab-pane");
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        lightDi.getListOfBeans(TabFactory.class)
                .stream()
                .filter(a -> a.isValueProvider())
                .forEach(tabFactory -> {
                    Tab tab = tabFactory.createTabContent();
                    tabPane.getTabs().add(tab);
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
