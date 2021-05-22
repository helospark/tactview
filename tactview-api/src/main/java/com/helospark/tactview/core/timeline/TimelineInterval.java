package com.helospark.tactview.core.timeline;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TimelineInterval {
    private static final BigDecimal EPSILON = new BigDecimal("0.0001");
    private final TimelinePosition startPosition;
    private final TimelineLength length;
    @JsonIgnore
    private final TimelinePosition endPosition;

    public TimelineInterval(@JsonProperty("startPosition") TimelinePosition startPosition, @JsonProperty("length") TimelineLength length) {
        this.startPosition = startPosition;
        this.length = length;
        this.endPosition = startPosition.add(length);
    }

    public TimelineInterval(TimelinePosition startPosition, TimelinePosition endPosition) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.length = TimelineLength.getLength(startPosition, endPosition);
    }

    public static TimelineInterval ofPoint(TimelinePosition position) {
        return new TimelineInterval(position, TimelineLength.ofZero());
    }

    public TimelinePosition getEndPosition() {
        return endPosition;
    }

    public TimelinePosition getStartPosition() {
        return startPosition;
    }

    public TimelineLength getLength() {
        return length;
    }

    public boolean contains(TimelinePosition position) {
        boolean isLargerThanStart = position.getSeconds().compareTo(startPosition.getSeconds()) >= 0;
        boolean isSmallerThanEnd = position.getSeconds().compareTo(endPosition.getSeconds()) <= 0;
        return isLargerThanStart && isSmallerThanEnd;
    }

    public boolean intersects(TimelineInterval intervalOfInterest) {
        boolean notIntersecting = intervalOfInterest.getEndPosition().isLessThan(this.getStartPosition()) ||
                intervalOfInterest.getStartPosition().isGreaterThan(this.getEndPosition());
        return !notIntersecting;
    }

    public TimelineInterval butWithStartPosition(TimelinePosition newStartPosition) {
        return new TimelineInterval(newStartPosition, this.endPosition);
    }

    public TimelineInterval butWithEndPosition(TimelinePosition newEndPosition) {
        return new TimelineInterval(this.startPosition, newEndPosition);
    }

    @Override
    public String toString() {
        return "TimelineInterval [startPosition=" + startPosition + ", length=" + length + ", endPosition=" + endPosition + "]";
    }

    public TimelineInterval butMoveStartPostionTo(TimelinePosition offsetStartPosition) {
        return new TimelineInterval(offsetStartPosition, offsetStartPosition.add(length));
    }

    public TimelineInterval butAddOffset(TimelinePosition offsetStartPosition) {
        return new TimelineInterval(this.startPosition.add(offsetStartPosition), length);
    }

    public TimelineInterval butMoveEndPostionTo(TimelinePosition position) {
        return new TimelineInterval(position.subtract(length), position);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endPosition == null) ? 0 : endPosition.hashCode());
        result = prime * result + ((startPosition == null) ? 0 : startPosition.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimelineInterval other = (TimelineInterval) obj;
        if (endPosition == null) {
            if (other.endPosition != null)
                return false;
        } else if (!endPosition.equals(other.endPosition))
            return false;
        if (startPosition == null) {
            if (other.startPosition != null)
                return false;
        } else if (!startPosition.equals(other.startPosition))
            return false;
        return true;
    }

    public static TimelineInterval fromDoubles(double visibleStartPosition, double visibleEndPosition) {
        return new TimelineInterval(TimelinePosition.ofSeconds(visibleStartPosition), TimelinePosition.ofSeconds(visibleEndPosition));
    }

    public boolean isEqualWithEpsilon(TimelineInterval other) {
        return this.getStartPosition().subtract(other.getStartPosition()).getSeconds().abs().compareTo(EPSILON) < 0 &&
                this.getEndPosition().subtract(other.getEndPosition()).getSeconds().abs().compareTo(EPSILON) < 0;

    }

}
