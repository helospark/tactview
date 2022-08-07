package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.util.DesSerFactory;

public class ValueListProvider<T extends ValueListElement> extends KeyframeableEffect<T> {
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
    public T getValueWithoutScriptAt(TimelinePosition position) {
        String id = stringInterpolator.valueAt(position);
        return elements.get(id);
    }

    @Override
    public T getValueAt(TimelinePosition position, EvaluationContext evaluationContext) {
        if (expression != null && evaluationContext != null) {
            String expressionResult = evaluationContext.evaluateExpression(expression, position, String.class);
            if (expressionResult == null || elements.get(expressionResult) == null) {
                return getValueWithoutScriptAt(position);
            } else {
                return elements.get(expressionResult);
            }
        } else {
            return getValueWithoutScriptAt(position);
        }
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, T value) {
        stringInterpolator.valueAdded(globalTimelinePosition, value.getId());
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
    public ValueListProvider<T> deepCloneInternal(CloneRequestMetadata cloneRequestMetadata) {
        return new ValueListProvider<>(new ArrayList<>(elements.values()), stringInterpolator.deepClone(cloneRequestMetadata));
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

    @Override
    public EffectInterpolator getInterpolator() {
        return stringInterpolator;
    }

    @Override
    public Class<?> getProvidedType() {
        return ValueListElement.class;
    }
}
