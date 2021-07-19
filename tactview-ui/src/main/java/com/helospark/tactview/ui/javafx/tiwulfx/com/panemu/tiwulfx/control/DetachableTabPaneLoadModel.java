package com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DetachableTabPaneLoadModel {
    TabPaneElement root;

    List<TabPaneElement> childrenWindows = List.of();

    public DetachableTabPaneLoadModel(@JsonProperty("root") TabPaneElement root) {
        this.root = root;
    }

    public TabPaneElement getRoot() {
        return root;
    }

    public void setRoot(TabPaneElement root) {
        this.root = root;
    }

}
