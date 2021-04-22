package com.helospark.tactview.core.timeline;

import java.math.BigDecimal;
import java.util.List;

public class DirtyIntervalList {
    private NonIntersectingIntervalList<IntervalContainer> knownIntervals = new NonIntersectingIntervalList<>();

    public long positionLastModified(TimelinePosition position) {
        return knownIntervals.getElementWithIntervalContainingPoint(position)
                .map(container -> container.lastModified)
                .orElse(0L);
    }

    public void dirtyInterval(TimelineInterval interval) {
        synchronized (knownIntervals) {
            List<IntervalContainer> intersections = knownIntervals.computeIntersectingIntervals(interval);
            knownIntervals.removeAll(intersections);
            TimelineInterval mergedInterval = mergeIntervals(interval, intersections);
            IntervalContainer newInterval = new IntervalContainer(mergedInterval, System.currentTimeMillis());
            knownIntervals.addInterval(newInterval);
        }
    }

    private TimelineInterval mergeIntervals(TimelineInterval interval, List<IntervalContainer> intervalsToReaddForStartPosition) {
        BigDecimal minStartPosition = interval.getStartPosition().getSeconds();
        BigDecimal maxEndPosition = interval.getEndPosition().getSeconds();
        for (var currentInterval : intervalsToReaddForStartPosition) {
            BigDecimal currentStartSeconds = currentInterval.getInterval().getStartPosition().getSeconds();
            BigDecimal currentEndSeconds = currentInterval.getInterval().getEndPosition().getSeconds();
            if (currentStartSeconds.compareTo(minStartPosition) < 0) {
                minStartPosition = currentStartSeconds;
            }
            if (currentEndSeconds.compareTo(maxEndPosition) > 0) {
                maxEndPosition = currentEndSeconds;
            }
        }
        return new TimelineInterval(new TimelinePosition(minStartPosition), new TimelinePosition(maxEndPosition));
    }

    static class IntervalContainer implements IntervalAware {
        private TimelineInterval interval;
        private long lastModified;

        public IntervalContainer(TimelineInterval interval, long lastModified) {
            this.interval = interval;
            this.lastModified = lastModified;
        }

        @Override
        public TimelineInterval getInterval() {
            return interval;
        }

        public long getLastModified() {
            return lastModified;
        }

        @Override
        public String toString() {
            return "IntervalContainer [interval=" + interval + ", lastModified=" + lastModified + "]";
        }

    }
}
