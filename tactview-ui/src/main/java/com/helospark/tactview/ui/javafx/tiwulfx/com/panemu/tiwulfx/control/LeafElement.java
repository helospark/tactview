package com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control;

import java.util.ArrayList;
import java.util.List;

public class LeafElement extends TabPaneElement {
    public List<DetachableTab> tabs = new ArrayList<>();

    public LeafElement(List<DetachableTab> tabs) {
        super("leaf");
        this.tabs = tabs;
    }

    public LeafElement() {
        super("leaf");
    }

}
