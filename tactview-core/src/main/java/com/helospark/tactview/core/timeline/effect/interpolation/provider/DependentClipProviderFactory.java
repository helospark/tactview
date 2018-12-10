package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.SavedContentAddable;

public class DependentClipProviderFactory implements DesSerFactory<DependentClipProvider> {

    @Override
    public void addDataForDeserialize(DependentClipProvider instance, Map<String, Object> data) {
        data.put("stringInterpolator", instance.stringInterpolator);
    }

    @Override
    public DependentClipProvider deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue) {
        return new DependentClipProvider(ReflectionUtil.deserialize(data.get("stringInterpolator"), StringInterpolator.class));
    }

}
