package com.helospark.tactview.core.decoder;

import com.helospark.tactview.core.timeline.TimelineLength;

public abstract class VisualMediaMetadata {
    protected int width;
    protected int height;
    protected TimelineLength length;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public TimelineLength getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "VisualMediaMetadata [width=" + width + ", height=" + height + ", length=" + length + "]";
    }

}
