package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class MultiKeyframeBasedDoubleInterpolator implements KeyframeSupportingDoubleInterpolator {
    private TreeMap<TimelinePosition, Double> values;
    private UnivariateInterpolator interpolatorImplementation = new LinearInterpolator();
    private double defaultValue;

    public MultiKeyframeBasedDoubleInterpolator(Double singleDefaultValue) {
        this.values = new TreeMap<>();
        this.defaultValue = singleDefaultValue;
    }

    public MultiKeyframeBasedDoubleInterpolator(Double singleDefaultValue, UnivariateInterpolator interpolatorImplementation) {
        this.values = new TreeMap<>();
        this.defaultValue = singleDefaultValue;
        this.interpolatorImplementation = interpolatorImplementation;
    }

    public MultiKeyframeBasedDoubleInterpolator(TimelinePosition singleDefaultKey, Double singleDefaultValue) {
        this.values = new TreeMap<>();
        values.put(singleDefaultKey, singleDefaultValue);
    }

    public MultiKeyframeBasedDoubleInterpolator(TimelinePosition singleDefaultKey, Double singleDefaultValue, UnivariateInterpolator interpolatorImplementation) {
        this.values = new TreeMap<>();
        values.put(singleDefaultKey, singleDefaultValue);
        this.interpolatorImplementation = interpolatorImplementation;
    }

    public MultiKeyframeBasedDoubleInterpolator(TreeMap<TimelinePosition, Double> values) {
        this.values = values;
    }

    @Override
    public Double valueAt(TimelinePosition position) {
        Entry<TimelinePosition, Double> lastEntry = values.lastEntry();
        Entry<TimelinePosition, Double> firstEntry = values.firstEntry();
        if (values.isEmpty()) {
            return defaultValue;
        } else if (values.size() == 1) {
            return values.firstEntry().getValue();
        } else if (position.isGreaterThan(lastEntry.getKey())) {
            return lastEntry.getValue();
        } else if (position.isLessThan(firstEntry.getKey())) {
            return firstEntry.getValue();
        } else {
            return interpolatorImplementation.interpolate(getKeysAsSeconds(values), getValuesAsDouble(values))
                    .value(position.getSeconds().doubleValue());
        }
    }

    private double[] getValuesAsDouble(TreeMap<TimelinePosition, Double> values) {
        return values.values()
                .stream()
                .mapToDouble(Double::valueOf)
                .toArray();
    }

    private double[] getKeysAsSeconds(TreeMap<TimelinePosition, Double> values) {
        return values.keySet()
                .stream()
                .map(key -> key.getSeconds())
                .map(key -> key.doubleValue())
                .mapToDouble(Double::valueOf)
                .toArray();
    }

    @Override
    public void valueAdded(TimelinePosition globalTimelinePosition, String value) {
        values.put(globalTimelinePosition, Double.valueOf(value));
    }

    @Override
    public void valueRemoved(TimelinePosition globalTimelinePosition) {
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
    public MultiKeyframeBasedDoubleInterpolator cloneInterpolator() {
        TreeMap<TimelinePosition, Double> newValues = new TreeMap<>(values);
        UnivariateInterpolator newInterpolatorImplementation = interpolatorImplementation;
        MultiKeyframeBasedDoubleInterpolator result = new MultiKeyframeBasedDoubleInterpolator(defaultValue);
        result.values = newValues;
        result.interpolatorImplementation = newInterpolatorImplementation;
        return result;
    }

}
