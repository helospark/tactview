package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class BooleanProviderFactory implements DesSerFactory<BooleanProvider> {

    @Override
    public void addDataForDeserialize(BooleanProvider instance, Map<String, Object> data) {
        data.put("interpolator", instance.doubleInterpolator);
    }

    @Override
    public BooleanProvider deserialize(Map<String, Object> data) {
        return new BooleanProvider((DoubleInterpolator) data.get("interpolator"));
    }

}
