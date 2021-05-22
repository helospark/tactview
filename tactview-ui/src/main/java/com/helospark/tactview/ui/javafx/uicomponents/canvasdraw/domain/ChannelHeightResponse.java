package com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain;

import com.helospark.tactview.core.timeline.TimelineChannel;

public class ChannelHeightResponse {
    public double top;
    public double bottom;
    public TimelineChannel channel;

    public ChannelHeightResponse(double top, double bottom, TimelineChannel channel) {
        this.top = top;
        this.bottom = bottom;
        this.channel = channel;
    }

}
