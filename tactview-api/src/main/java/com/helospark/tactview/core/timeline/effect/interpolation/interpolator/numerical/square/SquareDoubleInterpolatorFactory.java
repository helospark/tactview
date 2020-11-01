package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.square;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;

public class SquareDoubleInterpolatorFactory implements DesSerFactory<SquareDoubleInterpolator> {

    @Override
    public void serializeInto(SquareDoubleInterpolator instance, Map<String, Object> data, SaveMetadata saveMetadata) {
        data.put("minValue", instance.minValue);
        data.put("maxValue", instance.maxValue);
        data.put("onTime", instance.onTime);
        data.put("offTime", instance.offTime);

        data.put("initialOnTime", instance.initialOnTime);
        data.put("initialOffTime", instance.initialOffTime);
        data.put("initialMinValue", instance.initialMinValue);
        data.put("initialMaxValue", instance.initialMaxValue);
    }

    @Override
    public SquareDoubleInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            double minValue = data.get("minValue").asDouble();
            double maxValue = data.get("maxValue").asDouble();
            double onTime = data.get("onTime").asDouble();
            double offTime = data.get("offTime").asDouble();

            SquareDoubleInterpolator result = SquareDoubleInterpolator.builder()
                    .withMaxValue(maxValue)
                    .withMinValue(minValue)
                    .withOnTime(onTime)
                    .withOffTime(offTime)
                    .build();

            result.initialOnTime = data.get("initialOnTime").asDouble();
            result.initialOffTime = data.get("initialOffTime").asDouble();
            result.initialMinValue = data.get("initialMinValue").asDouble();
            result.initialMaxValue = data.get("initialMaxValue").asDouble();

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
