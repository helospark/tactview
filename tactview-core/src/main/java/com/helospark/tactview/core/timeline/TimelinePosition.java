package com.helospark.tactview.core.timeline;

import java.math.BigDecimal;

public class TimelinePosition {
    private BigDecimal seconds;

    public TimelinePosition(BigDecimal seconds) {
        this.seconds = seconds;
    }

    public TimelinePosition add(TimelineLength length) {
        return new TimelinePosition(seconds.add(length.getSeconds()));
    }

    public BigDecimal getSeconds() {
        return seconds;
    }

    public boolean isLessThan(TimelinePosition other) {
        return this.getSeconds().compareTo(other.getSeconds()) < 0;
    }
}
