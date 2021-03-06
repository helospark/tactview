package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier;

import static com.helospark.tactview.core.util.StaticObjectMapper.objectMapper;

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;
import com.helospark.tactview.core.util.bezier.CubicBezierPoint;

public class BezierDoubleInterpolatorInterpolatorFactory implements DesSerFactory<BezierDoubleInterpolator> {

    @Override
    public void serializeInto(BezierDoubleInterpolator instance, Map<String, Object> data, SaveMetadata saveMetadata) {
        data.put("defaultValue", instance.defaultValue);
        data.put("values", instance.values);
        data.put("useKeyframes", instance.useKeyframes);

        data.put("initialValues", instance.initialValues);
        data.put("initialDefaultValue", instance.initialDefaultValue);
    }

    @Override
    public BezierDoubleInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            Double defaultValue = data.get("defaultValue").asDouble();
            TreeMap<TimelinePosition, CubicBezierPoint> values = objectMapper.readValue(
                    objectMapper.treeAsTokens(data.get("values")),
                    objectMapper.getTypeFactory().constructType(new TypeReference<TreeMap<TimelinePosition, CubicBezierPoint>>() {
                    }));

            Double initialDefaultValue = data.get("initialDefaultValue").asDouble();
            TreeMap<TimelinePosition, CubicBezierPoint> initialValues = objectMapper.readValue(
                    objectMapper.treeAsTokens(data.get("initialValues")),
                    objectMapper.getTypeFactory().constructType(new TypeReference<TreeMap<TimelinePosition, CubicBezierPoint>>() {
                    }));

            BezierDoubleInterpolator result = new BezierDoubleInterpolator(defaultValue);
            result.values = new TreeMap<>(values);
            result.useKeyframes = data.get("useKeyframes").asBoolean();

            result.initialValues = new TreeMap<>(initialValues);
            result.initialDefaultValue = initialDefaultValue;

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
