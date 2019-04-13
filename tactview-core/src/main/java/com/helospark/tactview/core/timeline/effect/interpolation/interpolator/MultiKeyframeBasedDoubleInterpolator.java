package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.DesSerFactory;

public class MultiKeyframeBasedDoubleInterpolator extends KeyframeSupportingDoubleInterpolator {
    protected TreeMap<TimelinePosition, Double> values;
    protected UnivariateInterpolator interpolatorImplementation = new LinearInterpolator();
    protected double defaultValue;
    protected boolean useKeyframes;

    public MultiKeyframeBasedDoubleInterpolator(Double singleDefaultValue) {
        this.values = new TreeMap<>();
        this.defaultValue = singleDefaultValue;
    }

    public MultiKeyframeBasedDoubleInterpolator(Double singleDefaultValue, UnivariateInterpolator interpolatorImplementation) {
        this.values = new TreeMap<>();
        this.defaultValue = singleDefaultValue;
        this.interpolatorImplementation = interpolatorImplementation;
    }

    public MultiKeyframeBasedDoubleInterpolator(TreeMap<TimelinePosition, Double> values) {
        this.values = values;
    }

    @Override
    public Double valueAt(TimelinePosition position) {
        Entry<TimelinePosition, Double> lastEntry = values.lastEntry();
        Entry<TimelinePosition, Double> firstEntry = values.firstEntry();
        if (values.isEmpty() || !useKeyframes) {
            return defaultValue;
        } else if (values.size() == 1) {
            return values.firstEntry().getValue();
        } else if (position.isGreaterThan(lastEntry.getKey())) {
            return lastEntry.getValue();
        } else if (position.isLessThan(firstEntry.getKey())) {
            return firstEntry.getValue();
        } else {
            return doInterpolate(position);
        }
    }

    protected Double doInterpolate(TimelinePosition position) {
        return interpolatorImplementation.interpolate(getKeys(values), getValuesAsDouble(values))
                .value(position.getSeconds().doubleValue());
    }

    protected double[] getValuesAsDouble(TreeMap<TimelinePosition, Double> values) {
        return values.values()
                .stream()
                .mapToDouble(Double::valueOf)
                .toArray();
    }

    protected double[] getKeys(TreeMap<TimelinePosition, Double> values) {
        return values.keySet()
                .stream()
                .map(key -> key.getSeconds())
                .map(key -> key.doubleValue())
                .distinct() // just in case double representation of TimelinePosition is equals. // TODO: this causes exception due to dimension mismatch
                .mapToDouble(Double::valueOf)
                .toArray();
    }

    @Override
    public void valueAddedInternal(TimelinePosition globalTimelinePosition, String value) {
        Double valueToSet = Double.valueOf(value);
        if (!useKeyframes) {
            defaultValue = valueToSet;
        } else {
            values.put(globalTimelinePosition, valueToSet);
        }
    }

    @Override
    public void valueRemovedInternal(TimelinePosition globalTimelinePosition) {
        values.remove(globalTimelinePosition);
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        TreeMap<TimelinePosition, Object> result = new TreeMap<>();
        for (var entry : values.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public void setInterpolatorFunction(UnivariateInterpolator previousInterpolator) {
        this.interpolatorImplementation = previousInterpolator;
    }

    @Override
    public MultiKeyframeBasedDoubleInterpolator deepClone() {
        TreeMap<TimelinePosition, Double> newValues = new TreeMap<>(values);
        UnivariateInterpolator newInterpolatorImplementation = interpolatorImplementation;
        MultiKeyframeBasedDoubleInterpolator result = new MultiKeyframeBasedDoubleInterpolator(defaultValue);
        result.values = newValues;
        result.interpolatorImplementation = newInterpolatorImplementation;
        result.useKeyframes = useKeyframes;
        return result;
    }

    @Override
    public Class<? extends DesSerFactory<? extends EffectInterpolator>> generateSerializableContent() {
        return MultiKeyframeBasedDoubleInterpolatorFactory.class;
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
    public void setDefaultValue(double defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void valueModifiedAt(TimelinePosition timelinePosition, TimelinePosition newTime, double newValue) {
        if (useKeyframes) {
            values.remove(timelinePosition);
            values.put(newTime, newValue);
        }
    }

}
