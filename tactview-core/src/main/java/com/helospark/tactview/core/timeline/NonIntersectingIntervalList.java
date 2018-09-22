package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.List;

public class NonIntersectingIntervalList<T extends IntervalAware> {
    private List<T> intervalAwares;

    public boolean canAddInterval(TimelineInterval interval) {
        return computeIntersectingIntervals(interval).isEmpty();
    }

    public List<T> computeIntersectingIntervals(TimelineInterval interval) {
        List<T> result = new ArrayList<>(); // could be an emptylist, to avoid creating instances here unless necessary
        int index = 0;
        while (index < intervalAwares.size() && intervalEndIsLessThanOtherIntervalStartPosition(intervalAwares.get(index), interval)) {
            ++index;
        }
        while (index < intervalAwares.size() && intervalStartIsGreaterThanOtherIntervalEndPosition(intervalAwares.get(index), interval)) {
            result.add(result.get(index));
            ++index;
        }
        return result;
    }

    private boolean intervalEndIsLessThanOtherIntervalStartPosition(T intervalAware, TimelineInterval other) {
        return intervalAware.getInterval().getEndPosition().isLessThan(other.getStartPosition());
    }

    private boolean intervalStartIsGreaterThanOtherIntervalEndPosition(T intervalAware, TimelineInterval other) {
        return other.getEndPosition().isLessThan(intervalAware.getInterval().getStartPosition());
    }
}
