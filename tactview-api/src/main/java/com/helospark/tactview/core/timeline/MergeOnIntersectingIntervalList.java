package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Merges all elements into a single interval list that intersects.
 * @author helospark
 */
public class MergeOnIntersectingIntervalList implements Iterable<TimelineInterval> {
    private final List<TimelineInterval> intervalAwares = new ArrayList<>();

    public List<TimelineInterval> computeIntersectingIntervals(TimelineInterval interval) {
        List<TimelineInterval> result = new ArrayList<>(); // could be an emptylist, to avoid creating instances here unless necessary
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

    private boolean intervalEndIsLessThanOtherIntervalStartPosition(TimelineInterval intervalAware, TimelineInterval other) {
        return intervalAware.getEndPosition().isLessThan(other.getStartPosition());
    }

    private boolean intervalStartPositionIsLessThanOtherIntervalEndPosition(TimelineInterval intervalAware, TimelineInterval other) {
        return intervalAware.getStartPosition().isLessThanOrEqualTo(other.getEndPosition());
    }

    public Optional<TimelineInterval> getElementWithIntervalContainingPoint(TimelinePosition position) {
        for (int i = 0; i < intervalAwares.size(); ++i) {
            TimelineInterval current = intervalAwares.get(i);
            if (current.contains(position)) {
                return Optional.of(current);
            }
        }
        return Optional.empty();
    }

    public boolean addInterval(TimelineInterval clip) {
        List<TimelineInterval> intersectingIntervals = computeIntersectingIntervals(clip);

        TimelinePosition leftSide = clip.getStartPosition();
        TimelinePosition rightSide = clip.getEndPosition();

        for (var interval : intersectingIntervals) {
            TimelinePosition currentIntervalLeft = interval.getStartPosition();
            TimelinePosition currentIntervalRight = interval.getEndPosition();
            if (currentIntervalLeft.isLessThan(leftSide)) {
                leftSide = currentIntervalLeft;
            }
            if (currentIntervalRight.isGreaterThan(rightSide)) {
                rightSide = currentIntervalRight;
            }
        }

        intervalAwares.removeAll(intersectingIntervals);

        int i = 0;
        while (i < intervalAwares.size() && intervalAwares.get(i).getEndPosition().isLessOrEqualToThan(clip.getStartPosition())) {
            ++i;
        }
        intervalAwares.add(i, new TimelineInterval(leftSide, rightSide));
        return true;
    }

    @Override
    public Iterator<TimelineInterval> iterator() {
        return intervalAwares.iterator();
    }

    public Stream<TimelineInterval> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public int size() {
        return intervalAwares.size();
    }

    public TimelineInterval get(int index) {
        return intervalAwares.get(index);
    }

    @Override
    public String toString() {
        return intervalAwares.toString();
    }

}
