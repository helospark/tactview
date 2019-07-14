package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import static com.helospark.tactview.core.util.StaticObjectMapper.objectMapper;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.DesSerFactory;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.SavedContentAddable;

public class MultiKeyframeBasedDoubleInterpolatorFactory implements DesSerFactory<MultiKeyframeBasedDoubleInterpolator> {

    @Override
    public void addDataForDeserialize(MultiKeyframeBasedDoubleInterpolator instance, Map<String, Object> data) {
        data.put("defaultValue", instance.defaultValue);
        data.put("values", instance.values);
        data.put("useKeyframes", instance.useKeyframes);
        data.put("interpolatorImplementation", instance.interpolatorImplementation.getClass().getName());
    }

    @Override
    public MultiKeyframeBasedDoubleInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            Double defaultValue = data.get("defaultValue").asDouble();
            TreeMap<TimelinePosition, Double> values = objectMapper.readValue(
                    objectMapper.treeAsTokens(data.get("values")),
                    objectMapper.getTypeFactory().constructType(new TypeReference<TreeMap<TimelinePosition, Double>>() {
                    }));

            UnivariateInterpolator interpolator;
            interpolator = (UnivariateInterpolator) Class.forName(data.get("interpolatorImplementation").asText()).newInstance();
            MultiKeyframeBasedDoubleInterpolator result = new MultiKeyframeBasedDoubleInterpolator(defaultValue, interpolator);
            result.values = new TreeMap<>(values);
            result.useKeyframes = data.get("useKeyframes").asBoolean();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
