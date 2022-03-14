package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class MixedDoubleInterpolator extends KeyframeSupportingDoubleInterpolator {
    protected TreeMap<TimelinePosition, MixedDoubleInterpolatorElement> values;
    protected double defaultValue;
    protected boolean useKeyframes;

    protected TreeMap<TimelinePosition, MixedDoubleInterpolatorElement> initialValues;
    protected double initialDefaultValue;

    private EaseFunction defaultEaseFunction = EaseFunction.LINEAR;

    public MixedDoubleInterpolator(Double singleDefaultValue) {
        this.values = new TreeMap<>();
        this.defaultValue = singleDefaultValue;

        this.initialValues = cloneMap(values);
        this.initialDefaultValue = defaultValue;
    }

    public MixedDoubleInterpolator(TreeMap<TimelinePosition, MixedDoubleInterpolatorElement> values) {
        this.values = cloneMap(values);

        this.initialValues = cloneMap(this.values);
        this.initialDefaultValue = defaultValue;
    }

    private TreeMap<TimelinePosition, MixedDoubleInterpolatorElement> cloneMap(TreeMap<TimelinePosition, MixedDoubleInterpolatorElement> values) {
        TreeMap<TimelinePosition, MixedDoubleInterpolatorElement> result = new TreeMap<>();

        values.entrySet().stream()
                .forEach(entry -> result.put(entry.getKey(), entry.getValue().deepClone()));

        return result;
    }

    @Override
    public Double valueAt(TimelinePosition position) {
        if (values.isEmpty() || !useKeyframes) {
            return defaultValue;
        } else {
            SortedMap<TimelinePosition, MixedDoubleInterpolatorElement> headMap = values.headMap(position);
            SortedMap<TimelinePosition, MixedDoubleInterpolatorElement> tailMap = values.tailMap(position);

            TimelinePosition lowKey = headMap.isEmpty() ? null : headMap.lastKey();
            TimelinePosition highKey = tailMap.isEmpty() ? null : tailMap.firstKey();

            MixedDoubleInterpolatorElement headElement = lowKey == null ? null : values.get(lowKey);
            MixedDoubleInterpolatorElement tailElement = highKey == null ? null : values.get(highKey);

            if (headElement == null) {
                return tailElement.value;
            } else if (tailElement == null) {
                return headElement.value;
            } else {
                return doInterpolate(position, lowKey, highKey, headElement, tailElement);
            }
        }
    }

    private Double doInterpolate(TimelinePosition position, TimelinePosition lowKey, TimelinePosition highKey, MixedDoubleInterpolatorElement lowElement, MixedDoubleInterpolatorElement highElement) {
        PennerFunction function = lowElement.easeFunction.getFunction();

        double lowValue = lowElement.value;
        double highValue = highElement.value;

        double lowTime = lowKey.getSeconds().doubleValue();
        double highTime = highKey.getSeconds().doubleValue();
        double currentTime = position.getSeconds().doubleValue();

        double normalizedValue = (currentTime - lowTime) / (highTime - lowTime);

        return function.apply(normalizedValue, lowValue, highValue - lowValue, 1.0);
    }

    @Override
    public MixedDoubleInterpolator deepClone(CloneRequestMetadata cloneRequestMetadata) {
        MixedDoubleInterpolator result = new MixedDoubleInterpolator(values);
        result.defaultValue = defaultValue;
        result.useKeyframes = useKeyframes;
        result.defaultEaseFunction = defaultEaseFunction;
        return result;
    }

    @Override
    public Class<? extends DesSerFactory<? extends EffectInterpolator>> generateSerializableContent() {
        return MixedDoubleInterpolatorInterpolatorFactory.class;
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
    public void valueAddedInternal(TimelinePosition globalTimelinePosition, Double value) {
        Double valueToSet = value;
        if (!useKeyframes) {
            defaultValue = valueToSet;
        } else {
            values.put(globalTimelinePosition, new MixedDoubleInterpolatorElement(valueToSet, defaultEaseFunction));
        }
    }

    public void changeDefaultEaseFunction(EaseFunction defaultEaseFunction) {
        this.defaultEaseFunction = defaultEaseFunction;
    }

    public EaseFunction getDefaultEaseFunction() {
        return defaultEaseFunction;
    }

    @Override
    public void valueRemovedInternal(TimelinePosition globalTimelinePosition) {
        values.remove(globalTimelinePosition);
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        TreeMap<TimelinePosition, Object> result = new TreeMap<>();
        for (var entry : values.entrySet()) {
            result.put(entry.getKey(), entry.getValue().value);
        }
        return result;
    }

    public boolean hasEasingFunctionAt(TimelinePosition position) {
        return getEasingAt(position).isPresent();
    }

    public String getEasingFunctionAt(TimelinePosition position) {
        Entry<TimelinePosition, MixedDoubleInterpolatorElement> value = getEasingAt(position).orElseThrow();
        return value.getValue().easeFunction.getId();
    }

    public void changeEasingAt(TimelinePosition position, String newEasingId) {
        Entry<TimelinePosition, MixedDoubleInterpolatorElement> value = getEasingAt(position).orElseThrow();
        EaseFunction easeFunction = EaseFunction.fromId(newEasingId);
        value.getValue().easeFunction = easeFunction;
    }

    public Optional<Entry<TimelinePosition, MixedDoubleInterpolatorElement>> getEasingAt(TimelinePosition position) {
        Entry<TimelinePosition, MixedDoubleInterpolatorElement> value = values.headMap(position, true).lastEntry();
        return Optional.ofNullable(value);
    }

    @Override
    public void valueModifiedAt(TimelinePosition timelinePosition, TimelinePosition newTime, double newValue) {
        MixedDoubleInterpolatorElement originalValue = values.remove(timelinePosition);
        values.put(newTime, originalValue.butWithPoint(newValue));
    }

    @Override
    public void resetToDefaultValue() {
        this.values = new TreeMap<>(initialValues);
        this.defaultValue = initialDefaultValue;
    }

    @Override
    public double getDefaultValue() {
        return defaultValue;
    }

}
