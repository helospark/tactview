package com.helospark.tactview.core.timeline;

import java.math.BigDecimal;

public class TimelineLength {
    private static final BigDecimal MICROSECONDS = BigDecimal.valueOf(1000000L);

    private BigDecimal seconds;

    public TimelineLength(BigDecimal seconds) {
        this.seconds = seconds;
    }

    public static TimelineLength ofZero() {
        return new TimelineLength(BigDecimal.ZERO);
    }

    public BigDecimal getSeconds() {
        return seconds;
    }

    public static TimelineLength getLength(TimelinePosition startPosition, TimelinePosition endPosition) {
        return new TimelineLength(endPosition.getSeconds().subtract(endPosition.getSeconds()));
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

}
