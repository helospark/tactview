package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.SavedContentAddable;

public class ValueListProviderFactory implements DesSerFactory<ValueListProvider<?>> {

    @Override
    public void addDataForDeserialize(ValueListProvider<?> instance, Map<String, Object> data) {
        data.put("stringInterpolator", instance.stringInterpolator);
    }

    @Override
    public ValueListProvider<?> deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue) {
        ValueListProvider currentValueProvider = (ValueListProvider) currentFieldValue;
        StringInterpolator interpolator = ReflectionUtil.deserialize(data.get("stringInterpolator"), StringInterpolator.class);
        return new ValueListProvider<ValueListElement>(new ArrayList<>((Collection<ValueListElement>) currentValueProvider.elements.values()), interpolator);
    }

}
