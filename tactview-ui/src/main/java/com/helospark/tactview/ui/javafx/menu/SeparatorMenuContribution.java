package com.helospark.tactview.ui.javafx.menu;

import java.util.ArrayList;
import java.util.List;

public class SeparatorMenuContribution implements MenuContribution {
    private List<String> path;

    public SeparatorMenuContribution(List<String> path) {
        this.path = new ArrayList<>(path);
        this.path.add("----"); // separator has no name
    }

    @Override
    public List<String> getPath() {
        return path;
    }
}
