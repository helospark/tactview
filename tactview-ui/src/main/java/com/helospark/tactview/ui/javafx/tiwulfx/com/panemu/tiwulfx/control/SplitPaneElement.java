package com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control;

import java.util.ArrayList;
import java.util.List;

public class SplitPaneElement extends TabPaneElement {
    public List<TabPaneElement> children = new ArrayList<>();
    public double[] size;
    public boolean isVertical;

    public SplitPaneElement() {
        super("splitPane");
    }

}
