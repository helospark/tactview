package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.factory;

import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;

public class GraphingMenuItemRequest {
    GraphProvider provider;
    double x, y;
    Runnable updateRunnable;

    public GraphingMenuItemRequest(GraphProvider provider, double x, double y, Runnable updateRunnable) {
        this.provider = provider;
        this.x = x;
        this.y = y;
        this.updateRunnable = updateRunnable;
    }

}
