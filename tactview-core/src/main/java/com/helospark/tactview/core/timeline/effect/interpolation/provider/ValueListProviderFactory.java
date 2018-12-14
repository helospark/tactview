package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.timeline.effect.interpolation.AbstractKeyframeableEffectDesSerFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.util.ReflectionUtil;

public class ValueListProviderFactory extends AbstractKeyframeableEffectDesSerFactory<ValueListProvider<?>> {

    @Override
    public void addDataForDeserializeInternal(ValueListProvider<?> instance, Map<String, Object> data) {
        data.put("stringInterpolator", instance.stringInterpolator);
    }

    @Override
    public ValueListProvider<?> deserializeInternal(JsonNode data, ValueListProvider<?> currentFieldValue) {
        ValueListProvider currentValueProvider = currentFieldValue;
        StringInterpolator interpolator = ReflectionUtil.deserialize(data.get("stringInterpolator"), StringInterpolator.class, currentFieldValue.stringInterpolator);
        return new ValueListProvider<ValueListElement>(new ArrayList<>(currentValueProvider.elements.values()), interpolator);
    }

}
