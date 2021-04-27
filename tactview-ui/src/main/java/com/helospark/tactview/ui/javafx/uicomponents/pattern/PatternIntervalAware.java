package com.helospark.tactview.ui.javafx.uicomponents.pattern;

import com.helospark.tactview.core.timeline.IntervalAware;
import com.helospark.tactview.core.timeline.TimelineInterval;

import javafx.scene.image.Image;

public class PatternIntervalAware implements IntervalAware {
    public Image image;
    public TimelineInterval interval;
    public double zoom;

    public PatternIntervalAware(Image image, TimelineInterval interval, double zoom) {
        this.image = image;
        this.interval = interval;
        this.zoom = zoom;
    }

    public Image getImage() {
        return image;
    }

    @Override
    public TimelineInterval getInterval() {
        return interval;
    }

    public double getZoom() {
        return zoom;
    }

}