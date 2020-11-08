package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElement;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class GraphNodeRemovedMessage implements AffectedModifiedIntervalAware {
    private GraphElement graphElement;
    TimelineInterval interval;

    public GraphNodeRemovedMessage(GraphElement graphElement, TimelineInterval interval) {
        this.graphElement = graphElement;
        this.interval = interval;
    }

    public GraphElement getGraphElement() {
        return graphElement;
    }

    @Override
    public String toString() {
        return "GraphNodeRemovedMessage [graphElement=" + graphElement + ", interval=" + interval + "]";
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(interval);
    }

}
