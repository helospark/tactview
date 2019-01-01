package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu;

import javafx.scene.control.MenuItem;

public interface PropertyValueContextMenuItem {

    public boolean supports(PropertyValueContextMenuRequest request);

    public MenuItem createMenuItem(PropertyValueContextMenuRequest request);

}
