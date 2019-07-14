package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.line;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.DesSerFactory;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.util.SavedContentAddable;

public class LineInterpolatorFactory implements DesSerFactory<LineDoubleInterpolator> {

    @Override
    public void addDataForDeserialize(LineDoubleInterpolator instance, Map<String, Object> data) {
        data.put("tangent", instance.tangent.toString());
        data.put("startValue", instance.startValue.toString());
    }

    @Override
    public LineDoubleInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            BigDecimal tangent = new BigDecimal(data.get("tangent").asText());
            BigDecimal startValue = new BigDecimal(data.get("startValue").asText());

            return new LineDoubleInterpolator(tangent, startValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
