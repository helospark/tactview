package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.AbstractKeyframeableEffectDesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;

public class LineProviderFactory extends AbstractKeyframeableEffectDesSerFactory<LineProvider> {

    @Override
    public void addDataForDeserializeInternal(LineProvider instance, Map<String, Object> data) {
        data.put("startPointProvider", instance.startPointProvider);
        data.put("endPointProvider", instance.endPointProvider);
    }

    @Override
    public LineProvider deserializeInternal(JsonNode data, LineProvider currentFieldValue, LoadMetadata loadMetadata) {
        return new LineProvider(ReflectionUtil.deserialize(data.get("startPointProvider"), PointProvider.class, currentFieldValue.startPointProvider, loadMetadata),
                ReflectionUtil.deserialize(data.get("endPointProvider"), PointProvider.class, currentFieldValue.endPointProvider, loadMetadata));
    }

}
