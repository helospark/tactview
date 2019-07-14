package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.helospark.tactview.core.DesSerFactory;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;

public class ValueListProvider<T extends ValueListElement> extends KeyframeableEffect {
    Map<String, T> elements;
    StepStringInterpolator stringInterpolator;

    public ValueListProvider(List<T> elements, StepStringInterpolator stringInterpolator) {
        this.elements = elements.stream()
                .collect(Collectors.toMap(a -> a.getId(), a -> a,
                        (v1, v2) -> {
                            throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        LinkedHashMap::new));
        this.stringInterpolator = stringInterpolator;
    }

    @Override
    public T getValueAt(TimelinePosition position) {
        String id = stringInterpolator.valueAt(position);
        return elements.get(id);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        stringInterpolator.valueAdded(globalTimelinePosition, value);
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        stringInterpolator.removeKeyframeAt(globalTimelinePosition);
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        return stringInterpolator.getValues();
    }

    public Map<String, T> getElements() {
        return elements;
    }

    @Override
    public ValueListProvider<T> deepClone() {
        return new ValueListProvider<T>(new ArrayList<T>(elements.values()), stringInterpolator.deepClone());
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect>> generateSerializableContent() {
        return ValueListProviderFactory.class;
    }

    @Override
    public boolean supportsKeyframes() {
        return stringInterpolator instanceof KeyframeSupportingInterpolator;
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        ((KeyframeSupportingInterpolator) stringInterpolator).setUseKeyframes(useKeyframes);
    }

    @Override
    public boolean keyframesEnabled() {
        return ((KeyframeSupportingInterpolator) stringInterpolator).isUsingKeyframes();
    }

}
