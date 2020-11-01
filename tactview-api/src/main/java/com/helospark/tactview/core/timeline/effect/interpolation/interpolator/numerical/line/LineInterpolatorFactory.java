package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.line;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;

public class LineInterpolatorFactory implements DesSerFactory<LineDoubleInterpolator> {

    @Override
    public void serializeInto(LineDoubleInterpolator instance, Map<String, Object> data, SaveMetadata saveMetadata) {
        data.put("tangent", instance.tangent.toString());
        data.put("startValue", instance.startValue.toString());

        data.put("initialTangent", instance.initialTangent);
        data.put("initialStartValue", instance.initialStartValue);
    }

    @Override
    public LineDoubleInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            BigDecimal tangent = new BigDecimal(data.get("tangent").asText());
            BigDecimal startValue = new BigDecimal(data.get("startValue").asText());

            LineDoubleInterpolator result = new LineDoubleInterpolator(tangent, startValue);

            result.initialTangent = new BigDecimal(data.get("initialTangent").asText());
            result.initialStartValue = new BigDecimal(data.get("initialStartValue").asText());

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
