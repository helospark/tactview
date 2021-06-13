package com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Tab;

public class LeafElement implements TabPaneElement {
    public List<Tab> tabs = new ArrayList<>();

    public LeafElement(List<Tab> tabs) {
        this.tabs = tabs;
    }

    public LeafElement() {
    }

}
