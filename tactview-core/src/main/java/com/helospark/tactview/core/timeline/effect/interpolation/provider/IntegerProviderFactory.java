package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class IntegerProviderFactory implements DesSerFactory<IntegerProvider> {

    @Override
    public void addDataForDeserialize(IntegerProvider instance, Map<String, Object> data) {
        data.put("min", instance.min);
        data.put("max", instance.max);
        data.put("interpolator", instance.interpolator);
    }

    @Override
    public IntegerProvider deserialize(Map<String, Object> data) {
        return new IntegerProvider((Integer) data.get("min"), (Integer) data.get("max"), (DoubleInterpolator) data.get("interpolator"));
    }

}
