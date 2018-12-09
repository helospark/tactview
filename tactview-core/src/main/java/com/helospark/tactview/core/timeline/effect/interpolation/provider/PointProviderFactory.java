package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.util.DesSerFactory;

public class PointProviderFactory implements DesSerFactory<PointProvider> {

    @Override
    public void addDataForDeserialize(PointProvider instance, Map<String, Object> data) {
        data.put("xProvider", instance.xProvider);
        data.put("yProvider", instance.yProvider);
    }

    @Override
    public PointProvider deserialize(Map<String, Object> data) {
        return new PointProvider((DoubleProvider) data.get("xProvider"), (DoubleProvider) data.get("yProvider"));
    }

}
