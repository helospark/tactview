package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class DoubleInterpolator implements EffectInterpolator {
    private TreeMap<TimelinePosition, Double> values;
    private UnivariateInterpolator interpolatorImplementation = new LinearInterpolator();
    private double defaultValue;

    public DoubleInterpolator(Double singleDefaultValue) {
        this.values = new TreeMap<>();
        this.defaultValue = singleDefaultValue;
    }

    public DoubleInterpolator(TimelinePosition singleDefaultKey, Double singleDefaultValue) {
        this.values = new TreeMap<>();
        values.put(singleDefaultKey, singleDefaultValue);
    }

    public DoubleInterpolator(TreeMap<TimelinePosition, Double> values) {
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

    public void valueAdded(TimelinePosition globalTimelinePosition, String value) {
        values.put(globalTimelinePosition, Double.valueOf(value));
    }

    public void valueRemoved(TimelinePosition globalTimelinePosition) {
        values.remove(globalTimelinePosition);
    }

}
