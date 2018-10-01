package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.Map.Entry;
import java.util.TreeMap;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class StringInterpolator implements EffectInterpolator {
    private TreeMap<TimelinePosition, String> values;

    public StringInterpolator(TreeMap<TimelinePosition, String> values) {
        this.values = values;
    }

    @Override
    public String valueAt(TimelinePosition position) {
        Entry<TimelinePosition, String> floorEntry = values.floorEntry(position);
        if (floorEntry == null) {
            return "";
        } else {
            return floorEntry.getValue();
        }
    }

    public void valueAdded(TimelinePosition globalTimelinePosition, String value) {
        values.put(globalTimelinePosition, value);
    }

    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        values.remove(globalTimelinePosition);
    }
}
