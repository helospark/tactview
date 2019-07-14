package com.helospark.tactview.core.timeline;

public interface IntervalAware {

    /**
     * Gets interval in parent.
     * 
     * @return interval in parent
     */
    public TimelineInterval getInterval();

    /**
     * Gets global position including parent
     * 
     * @return global position
     */
    public default TimelineInterval getGlobalInterval() {
        return getInterval();
    }

}
