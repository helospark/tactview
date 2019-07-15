package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.sine;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.DesSerFactory;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.util.SavedContentAddable;

public class SineDoubleInterpolatorFactory implements DesSerFactory<SineDoubleInterpolator> {

    @Override
    public void addDataForDeserialize(SineDoubleInterpolator instance, Map<String, Object> data) {
        data.put("frequency", instance.frequency);
        data.put("minValue", instance.minValue);
        data.put("maxValue", instance.maxValue);
        data.put("startOffset", instance.startOffset);
    }

    @Override
    public SineDoubleInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            double frequency = data.get("frequency").asDouble();
            double minValue = data.get("minValue").asDouble();
            double maxValue = data.get("maxValue").asDouble();
            double startOffset = data.get("startOffset").asDouble();

            return SineDoubleInterpolator.builder()
                    .withFrequency(frequency)
                    .withMaxValue(maxValue)
                    .withMinValue(minValue)
                    .withStartOffset(startOffset)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
