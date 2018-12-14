package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.AbstractKeyframeableEffectDesSerFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.util.ReflectionUtil;

public class DependentClipProviderFactory extends AbstractKeyframeableEffectDesSerFactory<DependentClipProvider> {

    @Override
    public void addDataForDeserializeInternal(DependentClipProvider instance, Map<String, Object> data) {
        data.put("stringInterpolator", instance.stringInterpolator);
    }

    @Override
    public DependentClipProvider deserializeInternal(JsonNode data, DependentClipProvider currentFieldValue, LoadMetadata loadMetadata) {
        return new DependentClipProvider(ReflectionUtil.deserialize(data.get("stringInterpolator"), StringInterpolator.class, currentFieldValue.stringInterpolator, loadMetadata));
    }

}
