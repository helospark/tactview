package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;

public class ValueListProvider<T extends ValueListElement> extends KeyframeableEffect {
    private Map<String, T> elements;
    private StringInterpolator stringInterpolator;

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
    public boolean hasKeyframes() {
        return stringInterpolator.hasKeyframes();
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

}
