package com.helospark.tactview.core.timeline;

import static java.math.RoundingMode.HALF_DOWN;

import java.math.BigDecimal;

public class TimelinePosition implements SecondsAware {
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

    public TimelinePosition from(TimelinePosition startPosition) {
        return new TimelinePosition(seconds.subtract(startPosition.seconds));
    }

    public static TimelinePosition fromFrameIndexWithFps(long frame, double fps) {
        return new TimelinePosition(new BigDecimal(frame).divide(new BigDecimal(fps), 3, HALF_DOWN));
    }

    public TimelinePosition add(BigDecimal increment) {
        return new TimelinePosition(this.getSeconds().add(increment));
    }

    public static TimelinePosition ofZero() {
        return new TimelinePosition(BigDecimal.ZERO);
    }

    public boolean isLessThan(int i) {
        return isLessThan(new TimelinePosition(new BigDecimal(i)));
    }
}
