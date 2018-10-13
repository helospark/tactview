package com.helospark.tactview.core.timeline;

import static java.math.RoundingMode.HALF_DOWN;

import java.math.BigDecimal;
import java.util.Objects;

public class TimelinePosition implements SecondsAware, Comparable<TimelinePosition> {
    private BigDecimal seconds;

    public TimelinePosition(BigDecimal seconds) {
        this.seconds = seconds;
    }

    public TimelinePosition(double seconds) {
        this(new BigDecimal(seconds));
    }

    public TimelinePosition add(TimelineLength length) {
        return new TimelinePosition(seconds.add(length.getSeconds()));
    }

    @Override
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

    public TimelinePosition add(TimelinePosition position) {
        return new TimelinePosition(this.getSeconds().add(position.getSeconds()));
    }

    public TimelineLength toLength() {
        return new TimelineLength(seconds);
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof TimelinePosition)) {
            return false;
        }
        TimelinePosition castOther = (TimelinePosition) other;
        return Objects.equals(seconds, castOther.seconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seconds);
    }

    @Override
    public String toString() {
        return "TimelinePosition [seconds=" + seconds + "]";
    }

    @Override
    public int compareTo(TimelinePosition o) {
        return this.getSeconds().compareTo(o.getSeconds());
    }

    public boolean isGreaterThan(TimelinePosition key) {
        return this.compareTo(key) > 0;
    }

    public boolean isLessOrEqualToThan(TimelinePosition other) {
        return this.getSeconds().compareTo(other.getSeconds()) <= 0;
    }

}
