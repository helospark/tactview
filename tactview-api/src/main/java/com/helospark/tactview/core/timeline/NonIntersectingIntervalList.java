package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NonIntersectingIntervalList<T extends IntervalAware> implements Iterable<T> {
    private final List<T> intervalAwares = new ArrayList<>();

    public boolean canAddInterval(TimelineInterval interval) {
        return computeIntersectingIntervals(interval).isEmpty();
    }

    public boolean canAddIntervalAtExcluding(TimelineInterval interval, List<T> exclusions) {
        List<T> intersectingIntervals = computeIntersectingIntervals(interval);
        intersectingIntervals.removeAll(exclusions);
        return intersectingIntervals.isEmpty();
    }

    public List<T> computeIntersectingIntervals(TimelineInterval interval) {
        List<T> result = new ArrayList<>(); // could be an emptylist, to avoid creating instances here unless necessary
        int index = 0;
        while (index < intervalAwares.size() && intervalEndIsLessThanOtherIntervalStartPosition(intervalAwares.get(index), interval)) {
            ++index;
        }
        while (index < intervalAwares.size() && intervalStartPositionIsLessThanOtherIntervalEndPosition(intervalAwares.get(index), interval)) {
            result.add(intervalAwares.get(index));
            ++index;
        }

        return result;
    }

    private boolean intervalEndIsLessThanOtherIntervalStartPosition(T intervalAware, TimelineInterval other) {
        return intervalAware.getInterval().getEndPosition().isLessOrEqualToThan(other.getStartPosition());
    }

    private boolean intervalStartPositionIsLessThanOtherIntervalEndPosition(T intervalAware, TimelineInterval other) {
        return intervalAware.getInterval().getStartPosition().isLessThan(other.getEndPosition());
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
    public boolean addInterval(T clip) {
        int i = 0;
        while (i < intervalAwares.size() && intervalAwares.get(i).getInterval().getEndPosition().isLessOrEqualToThan(clip.getInterval().getStartPosition())) {
            ++i;
        }
        intervalAwares.add(i, clip);
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return intervalAwares.iterator();
    }

    public T remove(T clip) {
        boolean result = intervalAwares.remove(clip);
        if (result == false) {
            throw new IllegalArgumentException("Remove was unsuccesful, because list no longer contains clip");
        } else {
            return clip;
        }
    }

    public T removeIndex(int index) {
        T result = intervalAwares.remove(index);
        return result;
    }

    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public boolean contains(StatelessEffect effect) {
        return intervalAwares.contains(effect);
    }

    public <H extends IntervalSettable> boolean resize(H clip, TimelineInterval newInterval) {
        if (newInterval.getLength().getSeconds().doubleValue() < 0.0) {
            return false;
        }
        this.remove((T) clip);
        if (canAddInterval(newInterval)) {
            clip.setInterval(newInterval);
            this.addInterval((T) clip);
            return true;
        } else {
            this.addInterval((T) clip);
            return false;
        }
    }

    public void removeAll(List<T> intersections) {
        intersections.stream()
                .forEach(a -> remove(a));
    }

    public int size() {
        return intervalAwares.size();
    }

    public T get(int index) {
        return intervalAwares.get(index);
    }

    @Override
    public String toString() {
        return "NonIntersectingIntervalList [intervalAwares=" + intervalAwares + "]";
    }

    public NonIntersectingIntervalList<T> shallowCopy() {
        NonIntersectingIntervalList<T> result = new NonIntersectingIntervalList<>();
        result.intervalAwares.addAll(intervalAwares);
        return result;
    }

}
