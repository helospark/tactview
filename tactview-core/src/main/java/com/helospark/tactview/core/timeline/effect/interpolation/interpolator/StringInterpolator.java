package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class StringInterpolator implements EffectInterpolator {
    private String defaultValue;
    private TreeMap<TimelinePosition, String> values;

    public StringInterpolator() {
        this.values = new TreeMap<>();
        this.defaultValue = "";
    }

    public StringInterpolator(String defaultValue) {
        this.values = new TreeMap<>();
        this.defaultValue = defaultValue;
    }

    public StringInterpolator(TreeMap<TimelinePosition, String> values) {
        this.values = values;
        this.defaultValue = "";
    }

    @Override
    public String valueAt(TimelinePosition position) {
        Entry<TimelinePosition, String> floorEntry = values.floorEntry(position);
        if (floorEntry == null) {
            return defaultValue;
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

    public boolean hasKeyframes() {
        return !values.isEmpty();
    }

    public Map<TimelinePosition, Object> getValues() {
        TreeMap<TimelinePosition, Object> result = new TreeMap<>();
        for (var entry : values.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public StringInterpolator deepClone() {
        TreeMap<TimelinePosition, String> newValues = new TreeMap<>(values);
        StringInterpolator result = new StringInterpolator(defaultValue);
        result.values = newValues;

        return result;
    }
}
