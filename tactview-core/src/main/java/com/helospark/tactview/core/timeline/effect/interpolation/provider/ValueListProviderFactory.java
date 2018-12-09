package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.util.DesSerFactory;

public class ValueListProviderFactory implements DesSerFactory<ValueListProvider<?>> {

    @Override
    public void addDataForDeserialize(ValueListProvider<?> instance, Map<String, Object> data) {
        data.put("stringInterpolator", instance.stringInterpolator);
        data.put("elements", instance.elements);
    }

    @Override
    public ValueListProvider<?> deserialize(Map<String, Object> data) {
        return null;
    }

}
