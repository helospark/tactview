package com.helospark.tactview.core.timeline.message;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public abstract class AbstractKeyframeChangedMessage implements AffectedModifiedIntervalAware {

    public abstract String getDescriptorId();

    public abstract TimelineInterval getInterval();

    public abstract String getContainingElementId();

}
