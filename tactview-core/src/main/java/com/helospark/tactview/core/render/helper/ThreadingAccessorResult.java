package com.helospark.tactview.core.render.helper;

import java.util.List;

import com.helospark.tactview.core.timeline.MergeOnIntersectingIntervalList;
import com.helospark.tactview.core.timeline.threading.SingleThreadedRenderable;

public class ThreadingAccessorResult {
    private final List<SingleThreadedRenderable> singleTheadRenderables;
    private final MergeOnIntersectingIntervalList singleThreadedIntervals;

    public ThreadingAccessorResult(List<SingleThreadedRenderable> singleTheadRenderables, MergeOnIntersectingIntervalList singleThreadedIntervals) {
        this.singleTheadRenderables = singleTheadRenderables;
        this.singleThreadedIntervals = singleThreadedIntervals;
    }

    public List<SingleThreadedRenderable> getSingleTheadRenderables() {
        return singleTheadRenderables;
    }

    public MergeOnIntersectingIntervalList getSingleThreadedIntervals() {
        return singleThreadedIntervals;
    }

}
