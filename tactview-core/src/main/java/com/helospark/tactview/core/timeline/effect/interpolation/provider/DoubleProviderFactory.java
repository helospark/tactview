package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class DoubleProviderFactory implements DesSerFactory<DoubleProvider> {

    @Override
    public void addDataForDeserialize(DoubleProvider instance, Map<String, Object> data) {
        data.put("sizeFunction", instance.sizeFunction);
        data.put("min", instance.min);
        data.put("max", instance.max);
        data.put("interpolator", instance.interpolator);
    }

    @Override
    public DoubleProvider deserialize(Map<String, Object> data) {
        DoubleProvider result = new DoubleProvider((Double) data.get("min"), (Double) data.get("max"),
                (DoubleInterpolator) data.get("interpolator"));
        result.sizeFunction = SizeFunction.valueOf((String) data.get("sizeFunction"));
        return result;
    }

}
