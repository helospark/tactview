package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.lightdi.annotation.Component;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.ScrollEvent;

@Component
public class TimeLineZoomCallback {
    double scale = 1.0; // not thread safe

    public void onScroll(ScrollEvent evt) {
        if (evt.isControlDown()) {
            evt.consume();
            double factor = 1.0;
            factor += evt.getDeltaY() > 0 ? 0.1 : -0.1;

            Node node = (Node) evt.getTarget();
            double x = evt.getX();

            double oldScale = node.getScaleX();
            scale = oldScale * factor;
            if (scale < 0.05)
                scale = 0.05;
            if (scale > 50)
                scale = 50;
            node.setScaleX(scale);

            double f = (scale / oldScale) - 1;
            Bounds bounds = node.localToScene(node.getBoundsInLocal());
            double dx = (x - (bounds.getWidth() / 2 + bounds.getMinX()));

            node.setTranslateX(node.getTranslateX() - f * dx);
        }
    }

    public double getScale() {
        return scale;
    }
}
