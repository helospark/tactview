package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;

/**
 * An interpolator that 
 * @author black
 *
 */
public class PercentAwareMultiKeyframeBasedDoubleInterpolator extends MultiKeyframeBasedDoubleInterpolator {
    private TimelineLength length;

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
    }

    @Override
    public Double valueAt(TimelinePosition nonScaledPosition) {
        Entry<TimelinePosition, Double> lastEntry = values.lastEntry();
        Entry<TimelinePosition, Double> firstEntry = values.firstEntry();
        TimelinePosition position = nonScaledPosition.divide(length);
        if (values.isEmpty()) {
            return defaultValue;
        } else if (values.size() == 1) {
            return values.firstEntry().getValue();
        } else if (position.isGreaterThan(lastEntry.getKey().divide(length))) {
            return lastEntry.getValue();
        } else if (position.isLessThan(firstEntry.getKey().divide(length))) {
            return firstEntry.getValue();
        } else {
            return doInterpolate(position);
        }
    }

    @Override
    protected double[] getKeys(TreeMap<TimelinePosition, Double> values) {
        double widthAsDouble = length.getSeconds().doubleValue();
        return Arrays.stream(super.getKeys(values))
                .map(a -> a / widthAsDouble)
                .toArray();
    }

    @Override
    public MultiKeyframeBasedDoubleInterpolator cloneInterpolator() {
        TreeMap<TimelinePosition, Double> newValues = new TreeMap<>(values);
        UnivariateInterpolator newInterpolatorImplementation = interpolatorImplementation;
        MultiKeyframeBasedDoubleInterpolator result = new PercentAwareMultiKeyframeBasedDoubleInterpolator(defaultValue, length);
        result.values = newValues;
        result.interpolatorImplementation = newInterpolatorImplementation;
        return result;
    }

    public void resizeTo(TimelineLength length) {
        BigDecimal changeInScale = length.getSeconds().divide(this.length.getSeconds(), 2, RoundingMode.HALF_UP);
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

}
