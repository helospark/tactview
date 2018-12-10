package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.SavedContentAddable;

public class StringProviderFactory implements DesSerFactory<StringProvider> {

    @Override
    public void addDataForDeserialize(StringProvider instance, Map<String, Object> data) {
        data.put("interpolator", instance.stringInterpolator);
    }

    @Override
    public StringProvider deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue) {
        return new StringProvider(ReflectionUtil.deserialize(data.get("interpolator"), StringInterpolator.class));
    }

}
