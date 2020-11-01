package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import static com.helospark.tactview.core.util.StaticObjectMapper.objectMapper;

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;

public class StepStringInterpolatorFactory implements DesSerFactory<StepStringInterpolator> {
    private ObjectMapper regularObjectMapper;

    public StepStringInterpolatorFactory() {
        regularObjectMapper = new ObjectMapper();
        regularObjectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        regularObjectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

    @Override
    public void serializeInto(StepStringInterpolator instance, Map<String, Object> data, SaveMetadata saveMetadata) {
        data.put("defaultValue", instance.defaultValue);
        data.put("values", instance.values);
        data.put("useKeyframes", instance.useKeyframes);

        data.put("initialDefaultValue", instance.initialDefaultValue);
        data.put("initialValues", instance.initialValues);
    }

    @Override
    public StepStringInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            StepStringInterpolator result = new StepStringInterpolator();

            String defaultValue = data.get("defaultValue").asText();
            String initialDefaultValue = data.get("initialDefaultValue").asText();
            TreeMap<TimelinePosition, String> values = objectMapper.readValue(
                    objectMapper.treeAsTokens(data.get("values")),
                    objectMapper.getTypeFactory().constructType(new TypeReference<TreeMap<TimelinePosition, String>>() {
                    }));
            TreeMap<TimelinePosition, String> initialValues = objectMapper.readValue(
                    objectMapper.treeAsTokens(data.get("initialValues")),
                    objectMapper.getTypeFactory().constructType(new TypeReference<TreeMap<TimelinePosition, String>>() {
                    }));

            result.defaultValue = defaultValue;
            result.values = values;
            result.initialValues = initialValues;
            result.initialDefaultValue = initialDefaultValue;
            result.useKeyframes = data.get("useKeyframes").asBoolean();

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
