package com.helospark.tactview.core.timeline;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DirtyIntervalList {
    private NonIntersectingIntervalList<IntervalContainer> clearIntervals = new NonIntersectingIntervalList<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    public long positionLastModified(TimelinePosition position) {
        return clearIntervals.getElementWithIntervalContainingPoint(position)
                .map(container -> container.lastModified)
                .orElse(0L);
    }

    public void dirtyInterval(TimelineInterval interval) {
        executorService.execute(() -> {
            List<IntervalContainer> intersections = clearIntervals.computeIntersectingIntervals(interval);
            List<IntervalContainer> intervalsToReaddForStartPosition = findIntersectingIntervalsAtPosition(interval.getStartPosition(), intersections);
            List<IntervalContainer> intervalsToReaddForEndPosition = findIntersectingIntervalsAtPosition(interval.getEndPosition(), intersections);
            clearIntervals.removeAll(intersections);
            clearIntervals.addInterval(new IntervalContainer(interval, System.currentTimeMillis()));
            intervalsToReaddForStartPosition.stream()
                    .forEach(a -> clearIntervals.addInterval(a));
            intervalsToReaddForEndPosition.stream()
                    .forEach(a -> clearIntervals.addInterval(a));
        });

    }

    private List<IntervalContainer> findIntersectingIntervalsAtPosition(TimelinePosition position, List<IntervalContainer> intersections) {
        return intersections.stream()
                .filter(interval -> interval.getInterval().contains(position))
                .map(interval -> new IntervalContainer(new TimelineInterval(interval.getInterval().getStartPosition(), position), interval.lastModified))
                .collect(Collectors.toList());
    }

    /*
    public void clearInterval(TimelineInterval interval) {
        executorService.execute(() -> {
            List<IntervalContainer> intersections = clearIntervals.computeIntersectingIntervals(interval);
            IntervalContainer mergedInterval = mergeIntervalContainer(interval, intersections);
            clearIntervals.removeAll(intersections);
            clearIntervals.addInterval(mergedInterval);
        });
    }
    
    private IntervalContainer mergeIntervalContainer(TimelineInterval interval, List<IntervalContainer> intersections) {
        BigDecimal minimalStartPoint = interval.getStartPosition().getSeconds();
        BigDecimal maximalEndPoint = interval.getEndPosition().getSeconds();
        for (IntervalContainer container : intersections) {
            BigDecimal currentStartPoint = container.getInterval().getStartPosition().getSeconds();
            BigDecimal currentEndPoint = container.getInterval().getEndPosition().getSeconds();
            if (currentStartPoint.compareTo(minimalStartPoint) < 0) {
                minimalStartPoint = currentStartPoint;
            }
            if (currentEndPoint.compareTo(maximalEndPoint) > 0) {
                maximalEndPoint = currentEndPoint;
            }
        }
        return new IntervalContainer(new TimelineInterval(new TimelinePosition(minimalStartPoint), new TimelinePosition(maximalEndPoint)), System.currentTimeMillis());
    }
    */

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

    }
}
