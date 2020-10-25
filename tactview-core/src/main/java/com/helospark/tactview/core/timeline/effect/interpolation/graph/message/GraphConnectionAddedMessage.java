package com.helospark.tactview.core.timeline.effect.interpolation.graph.message;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class GraphConnectionAddedMessage implements AffectedModifiedIntervalAware {
    private TimelineInterval affectedInterval;
    private ConnectionIndex startIndex;
    private ConnectionIndex endIndex;

    public GraphConnectionAddedMessage(TimelineInterval affectedInterval, ConnectionIndex startIndex, ConnectionIndex endIndex) {
        this.affectedInterval = affectedInterval;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public ConnectionIndex getStartIndex() {
        return startIndex;
    }

    public ConnectionIndex getEndIndex() {
        return endIndex;
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(affectedInterval);
    }

}
