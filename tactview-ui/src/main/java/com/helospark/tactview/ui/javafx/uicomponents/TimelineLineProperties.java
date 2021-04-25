package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class TimelineLineProperties {
    private boolean enabled;
    private String startChannel;
    private String endChannel;
    private TimelinePosition position;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStartChannel() {
        return startChannel;
    }

    public void setStartChannel(String startChannel) {
        this.startChannel = startChannel;
    }

    public String getEndChannel() {
        return endChannel;
    }

    public void setEndChannel(String endChannel) {
        this.endChannel = endChannel;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public void setPosition(TimelinePosition position) {
        this.position = position;
    }

    public void reset() {
        enabled = false;
    }

}
