package com.helospark.tactview.core.render.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.MergeOnIntersectingIntervalList;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.threading.SingleThreadedRenderable;

@Component
public class IntervalThreadingPartitioner {
    private final TimelineManagerAccessor timelineManagerAccessor;

    public IntervalThreadingPartitioner(TimelineManagerAccessor timelineManagerAccessor) {
        this.timelineManagerAccessor = timelineManagerAccessor;
    }

    public ThreadingAccessorResult partitionBasedOnRenderThreadability(TimelineInterval intervalOfInterest) {
        MergeOnIntersectingIntervalList mergedIntervals = new MergeOnIntersectingIntervalList();

        List<TimelineClip> allSingleThreadedClips = timelineManagerAccessor.getChannels()
                .stream()
                .flatMap(a -> a.getAllClips().stream())
                .filter(a -> a.getGlobalInterval().intersects(intervalOfInterest))
                .filter(a -> a instanceof SingleThreadedRenderable)
                .filter(a -> ((SingleThreadedRenderable) a).isSequentialRenderEnabled())
                .collect(Collectors.toList());

        allSingleThreadedClips.stream()
                .forEach(a -> mergedIntervals.addInterval(a.getGlobalInterval()));

        List<StatelessEffect> allSingleThreadedEffects = timelineManagerAccessor.getChannels()
                .stream()
                .flatMap(a -> a.getAllClips().stream())
                .flatMap(a -> a.getEffects().stream())
                .filter(a -> a.getGlobalInterval().intersects(intervalOfInterest))
                .filter(a -> a instanceof SingleThreadedRenderable)
                .filter(a -> ((SingleThreadedRenderable) a).isSequentialRenderEnabled())
                .collect(Collectors.toList());

        allSingleThreadedEffects.stream()
                .forEach(a -> mergedIntervals.addInterval(a.getGlobalInterval()));

        List<SingleThreadedRenderable> singleThreadRendables = new ArrayList<>();

        allSingleThreadedClips.stream()
                .forEach(a -> singleThreadRendables.add((SingleThreadedRenderable) a));
        allSingleThreadedEffects.stream()
                .forEach(a -> singleThreadRendables.add((SingleThreadedRenderable) a));

        return new ThreadingAccessorResult(singleThreadRendables, mergedIntervals);
    }
}
