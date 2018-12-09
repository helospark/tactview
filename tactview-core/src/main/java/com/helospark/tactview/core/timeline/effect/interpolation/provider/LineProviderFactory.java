package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.util.DesSerFactory;

public class LineProviderFactory implements DesSerFactory<LineProvider> {

    @Override
    public void addDataForDeserialize(LineProvider instance, Map<String, Object> data) {
        data.put("startPointProvider", instance.startPointProvider);
        data.put("endPointProvider", instance.endPointProvider);
    }

    @Override
    public LineProvider deserialize(Map<String, Object> data) {
        return new LineProvider((PointProvider) data.get("startPointProvider"), (PointProvider) data.get("endPointProvider"));
    }

}
