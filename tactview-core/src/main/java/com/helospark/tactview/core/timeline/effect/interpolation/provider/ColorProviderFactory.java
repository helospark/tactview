package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.util.DesSerFactory;

public class ColorProviderFactory implements DesSerFactory<ColorProvider> {

    @Override
    public void addDataForDeserialize(ColorProvider instance, Map<String, Object> data) {
        data.put("redProvider", instance.redProvider);
        data.put("greenProvider", instance.greenProvider);
        data.put("blueProvider", instance.blueProvider);
    }

    @Override
    public ColorProvider deserialize(Map<String, Object> data) {
        return new ColorProvider((DoubleProvider) data.get("redProvider"), (DoubleProvider) data.get("greenProvider"), (DoubleProvider) data.get("blueProvider"));
    }

}
