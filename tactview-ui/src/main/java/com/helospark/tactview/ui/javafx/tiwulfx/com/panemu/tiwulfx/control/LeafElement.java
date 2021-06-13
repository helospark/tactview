package com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control;

import java.util.ArrayList;
import java.util.List;

public class LeafElement implements TabPaneElement {
    public List<DetachableTab> tabs = new ArrayList<>();

    public LeafElement(List<DetachableTab> tabs) {
        this.tabs = tabs;
    }

    public LeafElement() {
    }

}
