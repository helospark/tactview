package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.AbstractKeyframeableEffectDesSerFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.util.ReflectionUtil;

public class BooleanProviderFactory extends AbstractKeyframeableEffectDesSerFactory<BooleanProvider> {

    @Override
    public void addDataForDeserializeInternal(BooleanProvider instance, Map<String, Object> data) {
        data.put("interpolator", instance.doubleInterpolator);
    }

    @Override
    public BooleanProvider deserializeInternal(JsonNode data, BooleanProvider currentFieldValue, LoadMetadata loadMetadata) {
        BooleanProvider result = new BooleanProvider(ReflectionUtil.deserialize(data.get("interpolator"), DoubleInterpolator.class, currentFieldValue.doubleInterpolator, loadMetadata));
        return result;
    }

}
