package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.DesSerFactory;

public class StepStringInterpolator implements StringInterpolator {
    String defaultValue;
    TreeMap<TimelinePosition, String> values;
    boolean useKeyframes;

    String initialDefaultValue;
    TreeMap<TimelinePosition, String> initialValues;

    public StepStringInterpolator() {
        this.values = new TreeMap<>();
        this.defaultValue = "";

        this.initialDefaultValue = defaultValue;
        this.initialValues = new TreeMap<>(values);
    }

    public StepStringInterpolator(String defaultValue) {
        this.values = new TreeMap<>();
        this.defaultValue = defaultValue;

        this.initialDefaultValue = defaultValue;
        this.initialValues = new TreeMap<>(values);
    }

    public StepStringInterpolator(TreeMap<TimelinePosition, String> values) {
        this.values = values;
        this.defaultValue = "";

        this.initialDefaultValue = defaultValue;
        this.initialValues = new TreeMap<>(initialValues);
    }

    public StepStringInterpolator(String defaultValue, TreeMap<TimelinePosition, String> values, boolean useKeyframes) {
        this.defaultValue = defaultValue;
        this.values = values;
        this.useKeyframes = useKeyframes;

        this.initialDefaultValue = defaultValue;
        this.initialValues = new TreeMap<>(initialValues);
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
    public StepStringInterpolator deepClone(CloneRequestMetadata cloneRequestMetadata) {
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

    @Override
    public void resetToDefaultValue() {
        this.defaultValue = initialDefaultValue;
        this.values = new TreeMap<>(initialValues);
    }
}
