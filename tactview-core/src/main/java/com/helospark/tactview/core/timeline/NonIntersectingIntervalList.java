package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class NonIntersectingIntervalList<T extends IntervalAware> implements Iterable<T> {
    private List<T> intervalAwares = new ArrayList<>();

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

    public Optional<T> getElementWithIntervalContainingPoint(TimelinePosition position) {
        for (int i = 0; i < intervalAwares.size(); ++i) {
            T current = intervalAwares.get(i);
            if (current.getInterval().contains(position)) {
                return Optional.of(current);
            }
        }
        return Optional.empty();
    }

    // TODO: not thread safe
    public void addInterval(T clip) {
        int i = 0;
        while (i < intervalAwares.size() && intervalAwares.get(i).getInterval().getEndPosition().isLessThan(clip.getInterval().getStartPosition())) {
            ++i;
        }
        intervalAwares.add(i, clip);
    }

    @Override
    public Iterator<T> iterator() {
        return intervalAwares.iterator();
    }

    public void remove(TimelineClip clip) {
        boolean result = intervalAwares.remove(clip);
        if (result == false) {
            throw new IllegalArgumentException("Remove was unsuccesful, because list no longer contains clip");
        }
    }

}
