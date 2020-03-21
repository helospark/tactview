package com.helospark.tactview.ui.javafx.uicomponents;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class TimelineLineProperties {
    private SimpleIntegerProperty startX = new SimpleIntegerProperty(0);
    private SimpleIntegerProperty startY = new SimpleIntegerProperty(0);

    private SimpleIntegerProperty endX = new SimpleIntegerProperty(0);
    private SimpleIntegerProperty endY = new SimpleIntegerProperty(0);

    private BooleanProperty enabledProperty = new SimpleBooleanProperty(false);

    public SimpleIntegerProperty getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX.set(startX);
    }

    public SimpleIntegerProperty getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY.set(startY);
    }

    public SimpleIntegerProperty getEndX() {
        return endX;
    }

    public void setEndX(int endX) {
        this.endX.set(endX);
    }

    public SimpleIntegerProperty getEndY() {
        return endY;
    }

    public void setEndY(int endY) {
        this.endY.set(endY);
    }

    public BooleanProperty getEnabledProperty() {
        return enabledProperty;
    }

    public void setEnabledProperty(boolean b) {
        this.enabledProperty.set(b);
    }

}
