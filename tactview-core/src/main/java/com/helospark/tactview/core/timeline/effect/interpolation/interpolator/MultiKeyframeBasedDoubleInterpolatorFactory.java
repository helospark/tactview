package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.DesSerFactory;

public class MultiKeyframeBasedDoubleInterpolatorFactory implements DesSerFactory<MultiKeyframeBasedDoubleInterpolator> {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void addDataForDeserialize(MultiKeyframeBasedDoubleInterpolator instance, Map<String, Object> data) {
        data.put("defaultValue", instance.defaultValue);
        data.put("values", instance.values);
        data.put("interpolatorImplementation", instance.interpolatorImplementation.getClass().getName());
    }

    @Override
    public MultiKeyframeBasedDoubleInterpolator deserialize(Map<String, Object> data) {
        try {
            Double defaultValue = (Double) data.get("defaultValue");
            TreeMap<TimelinePosition, Double> values = new TreeMap<>((Map) data.get("values"));
            UnivariateInterpolator interpolator;
            interpolator = (UnivariateInterpolator) Class.forName((String) data.get("interpolatorImplementation")).newInstance();
            MultiKeyframeBasedDoubleInterpolator result = new MultiKeyframeBasedDoubleInterpolator(defaultValue, interpolator);
            result.values = new TreeMap<>(values);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
