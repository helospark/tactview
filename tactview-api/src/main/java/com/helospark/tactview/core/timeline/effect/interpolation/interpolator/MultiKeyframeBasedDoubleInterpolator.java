package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.DesSerFactory;

public class MultiKeyframeBasedDoubleInterpolator extends KeyframeSupportingDoubleInterpolator {
    protected volatile TreeMap<TimelinePosition, Double> values;
    protected UnivariateInterpolator interpolatorImplementation = new LinearInterpolator();
    protected double defaultValue;
    protected boolean useKeyframes;

    protected double initializationDefaultValue;
    protected TreeMap<TimelinePosition, Double> initializationValues;

    public MultiKeyframeBasedDoubleInterpolator(Double singleDefaultValue) {
        this.values = new TreeMap<>();
        this.defaultValue = singleDefaultValue;

        this.initializationDefaultValue = this.defaultValue;
        this.initializationValues = new TreeMap<>(this.values);
    }

    public MultiKeyframeBasedDoubleInterpolator(Double singleDefaultValue, UnivariateInterpolator interpolatorImplementation) {
        this.values = new TreeMap<>();
        this.defaultValue = singleDefaultValue;
        this.interpolatorImplementation = interpolatorImplementation;

        this.initializationDefaultValue = this.defaultValue;
        this.initializationValues = new TreeMap<>(this.values);
    }

    public MultiKeyframeBasedDoubleInterpolator(TreeMap<TimelinePosition, Double> values) {
        this.values = values;

        this.initializationDefaultValue = this.defaultValue;
        this.initializationValues = new TreeMap<>(this.values);
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
        // This is used to deduplicate double keys (since TimelinePosition has more precision than double)
        Map<Double, Double> keyValueMap = new TreeMap<>();
        for (var entry : values.entrySet()) {
            keyValueMap.put(entry.getKey().getSeconds().doubleValue(), entry.getValue());
        }

        return interpolatorImplementation.interpolate(setToArray(keyValueMap.keySet()), setToArray(keyValueMap.values()))
                .value(position.getSeconds().doubleValue());
    }

    protected double[] setToArray(Collection<Double> keyValueMap) {
        Double[] wrapperResult = keyValueMap.toArray(new Double[keyValueMap.size()]);
        double[] result = new double[wrapperResult.length];

        for (int i = 0; i < wrapperResult.length; ++i) {
            result[i] = wrapperResult[i];
        }
        return result;
    }

    @Override
    public void valueAddedInternal(TimelinePosition globalTimelinePosition, Double value) {
        Double valueToSet = value;
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
    public MultiKeyframeBasedDoubleInterpolator deepClone(CloneRequestMetadata cloneRequestMetadata) {
        TreeMap<TimelinePosition, Double> newValues = new TreeMap<>(values);
        UnivariateInterpolator newInterpolatorImplementation = interpolatorImplementation;
        MultiKeyframeBasedDoubleInterpolator result = new MultiKeyframeBasedDoubleInterpolator(defaultValue);
        result.values = newValues;
        result.interpolatorImplementation = newInterpolatorImplementation;
        result.useKeyframes = useKeyframes;

        result.initializationDefaultValue = initializationDefaultValue;
        result.initializationValues = new TreeMap<>(initializationValues);

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

    @Override
    public void valueModifiedAt(TimelinePosition timelinePosition, TimelinePosition newTime, double newValue) {
        if (useKeyframes) {
            values.remove(timelinePosition);
            values.put(newTime, newValue);
        }
    }

    public UnivariateInterpolator getInterpolatorFunction() {
        return interpolatorImplementation;
    }

    @Override
    public void resetToDefaultValue() {
        this.defaultValue = initializationDefaultValue;
        this.values = new TreeMap<>(initializationValues);
    }

    @Override
    public double getDefaultValue() {
        return defaultValue;
    }

}
