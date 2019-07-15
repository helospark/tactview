package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.square;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.DesSerFactory;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.util.SavedContentAddable;

public class SquareDoubleInterpolatorFactory implements DesSerFactory<SquareDoubleInterpolator> {

    @Override
    public void addDataForDeserialize(SquareDoubleInterpolator instance, Map<String, Object> data) {
        data.put("minValue", instance.minValue);
        data.put("maxValue", instance.maxValue);
        data.put("onTime", instance.onTime);
        data.put("offTime", instance.offTime);
    }

    @Override
    public SquareDoubleInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            double minValue = data.get("minValue").asDouble();
            double maxValue = data.get("maxValue").asDouble();
            double onTime = data.get("onTime").asDouble();
            double offTime = data.get("offTime").asDouble();

            return SquareDoubleInterpolator.builder()
                    .withMaxValue(maxValue)
                    .withMinValue(minValue)
                    .withOnTime(onTime)
                    .withOffTime(offTime)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
