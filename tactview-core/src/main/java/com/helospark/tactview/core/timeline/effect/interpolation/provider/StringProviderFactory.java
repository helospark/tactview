package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class StringProviderFactory implements DesSerFactory<StringProvider> {

    @Override
    public void addDataForDeserialize(StringProvider instance, Map<String, Object> data) {
        data.put("interpolator", instance.stringInterpolator);
    }

    @Override
    public StringProvider deserialize(Map<String, Object> data) {
        return new StringProvider((StringInterpolator) data.get("interpolator"));
    }

}
