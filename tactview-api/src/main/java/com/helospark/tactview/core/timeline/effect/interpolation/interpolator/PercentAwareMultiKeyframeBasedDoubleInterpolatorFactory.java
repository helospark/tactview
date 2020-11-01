package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import static com.helospark.tactview.core.util.StaticObjectMapper.objectMapper;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;

public class PercentAwareMultiKeyframeBasedDoubleInterpolatorFactory implements DesSerFactory<PercentAwareMultiKeyframeBasedDoubleInterpolator> {

    @Override
    public void serializeInto(PercentAwareMultiKeyframeBasedDoubleInterpolator instance, Map<String, Object> data, SaveMetadata saveMetadata) {
        data.put("defaultValue", instance.defaultValue);
        data.put("values", instance.values);
        data.put("useKeyframes", instance.useKeyframes);
        data.put("interpolatorImplementation", instance.interpolatorImplementation.getClass().getName());
        data.put("currentLength", instance.length.getSeconds().toString());
    }

    @Override
    public PercentAwareMultiKeyframeBasedDoubleInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            Double defaultValue = data.get("defaultValue").asDouble();
            TreeMap<TimelinePosition, Double> values = objectMapper.readValue(
                    objectMapper.treeAsTokens(data.get("values")),
                    objectMapper.getTypeFactory().constructType(new TypeReference<TreeMap<TimelinePosition, Double>>() {
                    }));
            BigDecimal length = new BigDecimal(data.get("currentLength").asText());

            UnivariateInterpolator interpolator;
            interpolator = (UnivariateInterpolator) Class.forName(data.get("interpolatorImplementation").asText()).newInstance();
            PercentAwareMultiKeyframeBasedDoubleInterpolator result = new PercentAwareMultiKeyframeBasedDoubleInterpolator(defaultValue, new TimelineLength(length));
            result.values = new TreeMap<>(values);
            result.useKeyframes = data.get("useKeyframes").asBoolean();
            result.interpolatorImplementation = interpolator;
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
