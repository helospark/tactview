package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.DesSerFactory;

/**
 * An interpolator that 
 * @author black
 *
 */
public class PercentAwareMultiKeyframeBasedDoubleInterpolator extends MultiKeyframeBasedDoubleInterpolator {
    protected TimelineLength length;

    public PercentAwareMultiKeyframeBasedDoubleInterpolator(Double singleDefaultValue, TimelineLength width) {
        super(singleDefaultValue);
        this.length = width;
    }

    public PercentAwareMultiKeyframeBasedDoubleInterpolator(Double singleDefaultValue, UnivariateInterpolator interpolatorImplementation, TimelineLength width) {
        super(singleDefaultValue, interpolatorImplementation);
        this.length = width;
    }

    public PercentAwareMultiKeyframeBasedDoubleInterpolator(TreeMap<TimelinePosition, Double> values, TimelineLength width) {
        super(values);
        this.length = width;
        if (values.size() > 1) {
            setUseKeyframes(true);
        }
    }

    @Override
    public Double valueAt(TimelinePosition nonScaledPosition) {
        Entry<TimelinePosition, Double> lastEntry = values.lastEntry();
        Entry<TimelinePosition, Double> firstEntry = values.firstEntry();
        double[] keys = getKeys();
        TimelinePosition position = nonScaledPosition.divide(length);
        if (values.isEmpty() || !useKeyframes) {
            return defaultValue;
        } else if (values.size() == 1) {
            return values.firstEntry().getValue();
        } else if (position.getSeconds().doubleValue() >= keys[keys.length - 1]) {
            return lastEntry.getValue();
        } else if (position.getSeconds().doubleValue() <= keys[0]) {
            return firstEntry.getValue();
        } else {
            return doInterpolate(position);
        }
    }

    @Override
    protected Double doInterpolate(TimelinePosition position) {
        // This is used to deduplicate double keys (since TimelinePosition has more precision than double)
        Map<Double, Double> keyValueMap = getKeyValueMap();

        return interpolatorImplementation.interpolate(setToArray(keyValueMap.keySet()), setToArray(keyValueMap.values()))
                .value(position.getSeconds().doubleValue());
    }

    public double[] getKeys() {
        return setToArray(getKeyValueMap().keySet());
    }

    private Map<Double, Double> getKeyValueMap() {
        double widthAsDouble = length.getSeconds().doubleValue();
        Map<Double, Double> keyValueMap = new TreeMap<>();
        for (var entry : values.entrySet()) {
            keyValueMap.put(entry.getKey().getSeconds().doubleValue() / widthAsDouble, entry.getValue());
        }
        return keyValueMap;
    }

    @Override
    public MultiKeyframeBasedDoubleInterpolator deepClone() {
        TreeMap<TimelinePosition, Double> newValues = new TreeMap<>(values);
        UnivariateInterpolator newInterpolatorImplementation = interpolatorImplementation;
        MultiKeyframeBasedDoubleInterpolator result = new PercentAwareMultiKeyframeBasedDoubleInterpolator(defaultValue, length);
        result.values = newValues;
        result.interpolatorImplementation = newInterpolatorImplementation;
        result.useKeyframes = useKeyframes;
        return result;
    }

    public void resizeTo(TimelineLength length) {
        BigDecimal changeInScale = length.getSeconds().divide(this.length.getSeconds(), 10, RoundingMode.HALF_UP);
        TreeMap<TimelinePosition, Double> newValues = new TreeMap<>();
        for (var entry : values.entrySet()) {
            newValues.put(entry.getKey().multiply(changeInScale), entry.getValue());
        }
        this.values = newValues;
        this.length = length;
    }

    public void setLength(TimelineLength length) {
        this.length = length;
    }

    @Override
    public Class<? extends DesSerFactory<? extends EffectInterpolator>> generateSerializableContent() {
        return PercentAwareMultiKeyframeBasedDoubleInterpolatorFactory.class;
    }

}
