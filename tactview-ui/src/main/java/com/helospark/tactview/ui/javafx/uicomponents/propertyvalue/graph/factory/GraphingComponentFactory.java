package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.factory;

import javafx.scene.control.MenuItem;

public interface GraphingComponentFactory {

    public String getCategory();

    public MenuItem createMenuItem(GraphingMenuItemRequest request);

}
