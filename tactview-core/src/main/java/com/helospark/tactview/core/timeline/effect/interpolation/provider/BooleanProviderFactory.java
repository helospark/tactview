package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.SavedContentAddable;

public class BooleanProviderFactory implements DesSerFactory<BooleanProvider> {

    @Override
    public void addDataForDeserialize(BooleanProvider instance, Map<String, Object> data) {
        data.put("interpolator", instance.doubleInterpolator);
    }

    @Override
    public BooleanProvider deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue) {
        return new BooleanProvider(ReflectionUtil.deserialize(data.get("interpolator"), DoubleInterpolator.class));
    }

}
