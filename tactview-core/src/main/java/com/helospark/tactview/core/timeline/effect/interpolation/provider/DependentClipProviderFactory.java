package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class DependentClipProviderFactory implements DesSerFactory<DependentClipProvider> {

    @Override
    public void addDataForDeserialize(DependentClipProvider instance, Map<String, Object> data) {
        data.put("stringInterpolator", instance.stringInterpolator);
    }

    @Override
    public DependentClipProvider deserialize(Map<String, Object> data) {
        return new DependentClipProvider((StringInterpolator) data.get("stringInterpolator"));
    }

}
