package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.AbstractKeyframeableEffectDesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;

public class CurveProviderFactory extends AbstractKeyframeableEffectDesSerFactory<CurveProvider> {

    @Override
    public void addDataForDeserializeInternal(CurveProvider instance, Map<String, Object> data) {
        data.put("curvePoints", instance.curvePoints);

        data.put("maxX", instance.maxX);
        data.put("minX", instance.minX);

        data.put("maxY", instance.maxY);
        data.put("minY", instance.minY);

        data.put("isUsingKeyframes", instance.isUsingKeyframes);
    }

    @Override
    public CurveProvider deserializeInternal(JsonNode data, CurveProvider currentFieldValue, LoadMetadata loadMetadata) {
        try {
            List<PointProvider> curvePoints = new ArrayList<>();
            Iterator<JsonNode> iterator = data.get("curvePoints").iterator();
            while (iterator.hasNext()) {
                PointProvider point = ReflectionUtil.deserialize(iterator.next(), PointProvider.class, PointProvider.of(0, 0), loadMetadata);
                curvePoints.add(point);
            }
            double maxX = data.get("maxX").asDouble();
            double minX = data.get("minX").asDouble();
            double maxY = data.get("maxY").asDouble();
            double minY = data.get("minY").asDouble();
            boolean isUsingKeyframes = data.get("isUsingKeyframes").asBoolean();

            CurveProvider result = new CurveProvider(minX, maxX, minY, maxY, curvePoints);
            result.isUsingKeyframes = isUsingKeyframes;
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
