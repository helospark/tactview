package com.helospark.tactview.core.decoder;

import com.helospark.tactview.core.timeline.TimelineLength;

public abstract class VisualMediaMetadata extends MediaMetadata {
    protected int width;
    protected int height;
    protected boolean resizable;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isResizable() {
        return resizable;
    }

    @Override
    public TimelineLength getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "VisualMediaMetadata [width=" + width + ", height=" + height + ", resizable=" + resizable + "]";
    }

}
