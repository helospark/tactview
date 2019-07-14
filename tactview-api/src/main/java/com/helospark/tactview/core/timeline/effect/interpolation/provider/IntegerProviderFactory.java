package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.timeline.effect.interpolation.AbstractKeyframeableEffectDesSerFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;

public class IntegerProviderFactory extends AbstractKeyframeableEffectDesSerFactory<IntegerProvider> {

    @Override
    public void addDataForDeserializeInternal(IntegerProvider instance, Map<String, Object> data) {
        data.put("interpolator", instance.interpolator);
    }

    @Override
    public IntegerProvider deserializeInternal(JsonNode data, IntegerProvider currentFieldValue, LoadMetadata loadMetadata) {
        IntegerProvider current = currentFieldValue;

        IntegerProvider result = new IntegerProvider(current.getMin(), current.getMax(),
                ReflectionUtil.deserialize(data.get("interpolator"), DoubleInterpolator.class, currentFieldValue.interpolator, loadMetadata));

        return result;
    }

}
