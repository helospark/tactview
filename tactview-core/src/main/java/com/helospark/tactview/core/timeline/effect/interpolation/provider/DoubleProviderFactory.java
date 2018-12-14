package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.timeline.effect.interpolation.AbstractKeyframeableEffectDesSerFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.util.ReflectionUtil;

public class DoubleProviderFactory extends AbstractKeyframeableEffectDesSerFactory<DoubleProvider> {

    @Override
    public void addDataForDeserializeInternal(DoubleProvider instance, Map<String, Object> data) {
        data.put("interpolator", instance.interpolator);
    }

    @Override
    public DoubleProvider deserializeInternal(JsonNode data, DoubleProvider currentFieldValue) {

        DoubleProvider result = new DoubleProvider(currentFieldValue.getMin(), currentFieldValue.getMax(),
                ReflectionUtil.deserialize(data.get("interpolator"), DoubleInterpolator.class, currentFieldValue.interpolator));
        result.sizeFunction = currentFieldValue.getSizeFunction();
        return result;
    }

}
