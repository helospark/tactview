package com.helospark.tactview.ui.javafx.uicomponents.pattern;

import com.helospark.tactview.core.timeline.IntervalAware;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;

import javafx.scene.image.Image;

public class PatternIntervalAware implements IntervalAware {
    public Image image;
    public TimelineInterval interval;
    public TimelineLength length;
    public double zoom;

    public PatternIntervalAware(Image image, TimelineInterval interval, double zoom, TimelineLength length) {
        this.image = image;
        this.interval = interval;
        this.zoom = zoom;
        this.length = length;
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

    public TimelineLength getLength() {
        return length;
    }

}