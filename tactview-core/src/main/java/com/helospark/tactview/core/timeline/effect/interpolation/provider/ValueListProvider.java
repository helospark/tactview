package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class ValueListProvider<T extends ValueListElement> extends KeyframeableEffect {
    Map<String, T> elements;
    StringInterpolator stringInterpolator;

    public ValueListProvider(List<T> elements, StringInterpolator stringInterpolator) {
        this.elements = elements.stream()
                .collect(Collectors.toMap(a -> a.getId(), a -> a));
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

}
