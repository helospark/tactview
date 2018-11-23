package com.helospark.tactview.core.timeline;

import static java.math.RoundingMode.HALF_DOWN;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class TimelinePosition implements SecondsAware, Comparable<TimelinePosition> {
    private static final TimelinePosition ZERO_POSITION = new TimelinePosition(BigDecimal.ZERO);
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
        return ZERO_POSITION;
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

    public TimelinePosition subtract(SecondsAware endPosition) {
        return new TimelinePosition(this.getSeconds().subtract(endPosition.getSeconds()));
    }

    public BigDecimal distanceFrom(TimelinePosition position) {
        return seconds.subtract(position.seconds).abs();
    }

    public TimelinePosition multiply(BigDecimal rhs) {
        return new TimelinePosition(this.seconds.multiply(rhs));
    }

    public TimelinePosition divide(TimelineLength length) {
        return new TimelinePosition(this.seconds.divide(length.getSeconds(), 2, RoundingMode.HALF_UP));
    }

    public TimelineInterval toInterval() {
        return new TimelineInterval(this, TimelineLength.ofZero());
    }

    public TimelinePosition negate() {
        return new TimelinePosition(this.seconds.negate());
    }

}
