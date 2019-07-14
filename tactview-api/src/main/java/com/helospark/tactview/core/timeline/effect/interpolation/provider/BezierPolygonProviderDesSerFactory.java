package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import static com.helospark.tactview.core.util.StaticObjectMapper.objectMapper;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.DesSerFactory;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygonPoint;
import com.helospark.tactview.core.util.SavedContentAddable;

public class BezierPolygonProviderDesSerFactory implements DesSerFactory<BezierPolygonProvider> {

    @Override
    public void addDataForDeserialize(BezierPolygonProvider instance, Map<String, Object> data) {
        data.put("defaultValue", instance.defaultValues);
        data.put("values", instance.values);
        data.put("useKeyframes", instance.useKeyframes);
        data.put("interpolatorImplementation", instance.interpolatorImplementation.getClass().getName());
    }

    @Override
    public BezierPolygonProvider deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            List<BezierPolygonPoint> defaultValues = objectMapper.readValue(
                    objectMapper.treeAsTokens(data.get("defaultValue")),
                    objectMapper.getTypeFactory().constructType(new TypeReference<List<BezierPolygonPoint>>() {
                    }));
            TreeMap<TimelinePosition, List<BezierPolygonPoint>> values = objectMapper.readValue(
                    objectMapper.treeAsTokens(data.get("values")),
                    objectMapper.getTypeFactory().constructType(new TypeReference<TreeMap<TimelinePosition, List<BezierPolygonPoint>>>() {
                    }));

            UnivariateInterpolator interpolator;
            interpolator = (UnivariateInterpolator) Class.forName(data.get("interpolatorImplementation").asText()).newInstance();

            boolean useKeyframes = data.get("useKeyframes").asBoolean();

            return new BezierPolygonProvider(useKeyframes, defaultValues, values, interpolator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
