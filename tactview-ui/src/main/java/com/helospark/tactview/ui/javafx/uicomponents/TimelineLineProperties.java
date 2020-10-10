package com.helospark.tactview.ui.javafx.uicomponents;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class TimelineLineProperties {
    private SimpleDoubleProperty startX = new SimpleDoubleProperty(0);
    private SimpleDoubleProperty startY = new SimpleDoubleProperty(0);

    private SimpleDoubleProperty endX = new SimpleDoubleProperty(0);
    private SimpleDoubleProperty endY = new SimpleDoubleProperty(0);

    private BooleanProperty enabledProperty = new SimpleBooleanProperty(false);

    public SimpleDoubleProperty getStartX() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX.set(startX);
    }

    public SimpleDoubleProperty getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY.set(startY);
    }

    public SimpleDoubleProperty getEndX() {
        return endX;
    }

    public void setEndX(double endX) {
        this.endX.set(endX);
    }

    public SimpleDoubleProperty getEndY() {
        return endY;
    }

    public void setEndY(double endY) {
        this.endY.set(endY);
    }

    public BooleanProperty getEnabledProperty() {
        return enabledProperty;
    }

    public void setEnabledProperty(boolean b) {
        this.enabledProperty.set(b);
    }

}
