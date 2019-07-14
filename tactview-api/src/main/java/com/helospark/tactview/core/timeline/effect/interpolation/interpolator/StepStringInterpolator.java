package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.helospark.tactview.core.DesSerFactory;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class StepStringInterpolator implements StringInterpolator {
    String defaultValue;
    TreeMap<TimelinePosition, String> values;
    boolean useKeyframes;

    public StepStringInterpolator() {
        this.values = new TreeMap<>();
        this.defaultValue = "";
    }

    public StepStringInterpolator(String defaultValue) {
        this.values = new TreeMap<>();
        this.defaultValue = defaultValue;
    }

    public StepStringInterpolator(TreeMap<TimelinePosition, String> values) {
        this.values = values;
        this.defaultValue = "";
    }

    public StepStringInterpolator(String defaultValue, TreeMap<TimelinePosition, String> values, boolean useKeyframes) {
        this.defaultValue = defaultValue;
        this.values = values;
        this.useKeyframes = useKeyframes;
    }

    @Override
    public String valueAt(TimelinePosition position) {
        if (!useKeyframes) {
            return defaultValue;
        } else {
            Entry<TimelinePosition, String> floorEntry = values.floorEntry(position);
            if (floorEntry == null) {
                return defaultValue;
            } else {
                return floorEntry.getValue();
            }
        }
    }

    @Override
    public void valueAdded(TimelinePosition globalTimelinePosition, String value) {
        if (!useKeyframes) {
            this.defaultValue = value;
        } else {
            values.put(globalTimelinePosition, value);
        }
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        values.remove(globalTimelinePosition);
    }

    @Override
    public boolean hasKeyframes() {
        return !values.isEmpty();
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        TreeMap<TimelinePosition, Object> result = new TreeMap<>();
        for (var entry : values.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public StepStringInterpolator deepClone() {
        TreeMap<TimelinePosition, String> newValues = new TreeMap<>(values);
        StepStringInterpolator result = new StepStringInterpolator(defaultValue);
        result.values = newValues;

        return result;
    }

    @Override
    public Class<? extends DesSerFactory<? extends EffectInterpolator>> generateSerializableContent() {
        return StepStringInterpolatorFactory.class;
    }

    @Override
    public boolean useKeyframes() {
        return useKeyframes;
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        this.useKeyframes = useKeyframes;
    }

    @Override
    public boolean isUsingKeyframes() {
        return useKeyframes;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

}
