package com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control;

import java.util.ArrayList;
import java.util.List;

public class SplitPaneElement implements TabPaneElement {
    public List<TabPaneElement> children = new ArrayList<>();
    public double[] size;
    public boolean isVertical;
}
