package com.helospark.tactview.ui.javafx.tabs.dockabletab.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.uicomponents.PropertyView;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

@Component
public class PropertyEditorDockableTabFactory extends AbstractCachingDockableTabFactory {
    private static final String ID = "property-editor";
    private PropertyView effectPropertyView;

    public PropertyEditorDockableTabFactory(PropertyView effectPropertyView) {
        this.effectPropertyView = effectPropertyView;
    }

    private ScrollPane createPropertyPage() {
        VBox propertyBox = effectPropertyView.getPropertyWindow();
        ScrollPane propertyBoxScrollPane = new ScrollPane(propertyBox);
        propertyBoxScrollPane.setFitToWidth(true);
        propertyBoxScrollPane.setPrefWidth(500);
        return propertyBoxScrollPane;
    }

    @Override
    public DetachableTab createTabInternal() {
        return new DetachableTab("Property editor", createPropertyPage(), ID);
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
