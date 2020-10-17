package com.helospark.tactview.core.timeline;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TimelineLength implements SecondsAware {
    private static final BigDecimal MICROSECONDS = BigDecimal.valueOf(1000000L);

    private BigDecimal seconds;

    public TimelineLength(@JsonProperty("seconds") BigDecimal seconds) {
        this.seconds = seconds;
    }

    public static TimelineLength ofZero() {
        return new TimelineLength(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getSeconds() {
        return seconds;
    }

    public static TimelineLength getLength(TimelinePosition startPosition, TimelinePosition endPosition) {
        return new TimelineLength(endPosition.getSeconds().subtract(startPosition.getSeconds()));
    }

    public static TimelineLength ofMicroseconds(long lengthInMicroseconds) {
        return new TimelineLength(new BigDecimal(lengthInMicroseconds).divide(MICROSECONDS));
    }

    @Override
    public String toString() {
        return "TimelineLength [seconds=" + seconds + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((seconds == null) ? 0 : seconds.hashCode());
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
        TimelineLength other = (TimelineLength) obj;
        if (seconds == null) {
            if (other.seconds != null)
                return false;
        } else if (!seconds.equals(other.seconds))
            return false;
        return true;
    }

    public static TimelineLength ofMillis(int i) {
        return new TimelineLength(BigDecimal.valueOf(i).divide(BigDecimal.valueOf(1000)));
    }

    public boolean lessThan(TimelineLength length) {
        return this.getSeconds().compareTo(length.getSeconds()) < 0;
    }

    public boolean lessThanOrEqual(TimelineLength length) {
        return this.getSeconds().compareTo(length.getSeconds()) <= 0;
    }

    public boolean greaterThan(TimelineLength length) {
        return this.getSeconds().compareTo(length.getSeconds()) > 0;
    }

    public int compareTo(TimelineLength other) {
        return seconds.compareTo(other.seconds);
    }

    public TimelineLength multiply(int i) {
        return new TimelineLength(this.seconds.multiply(BigDecimal.valueOf(i)));
    }

    public static TimelineLength ofSeconds(double d) {
        return new TimelineLength(new BigDecimal(d));
    }

    public TimelinePosition toPosition() {
        return new TimelinePosition(this.seconds);
    }

    public static TimelineLength ofOne() {
        return new TimelineLength(BigDecimal.ONE);
    }

    public TimelineLength multiply(BigDecimal newLengthMultiplier) {
        return new TimelineLength(this.seconds.multiply(newLengthMultiplier));
    }

    public boolean isEquals(TimelineLength length) {
        BigDecimal epsilon = BigDecimal.valueOf(0.01);
        return this.getSeconds().subtract(length.getSeconds()).abs().compareTo(epsilon) < 0;
    }

}
