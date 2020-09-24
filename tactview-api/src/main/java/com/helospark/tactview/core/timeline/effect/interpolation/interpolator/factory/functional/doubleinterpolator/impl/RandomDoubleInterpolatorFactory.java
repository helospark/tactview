package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.RepeatableRandom;
import com.helospark.tactview.core.util.SavedContentAddable;

public class RandomDoubleInterpolatorFactory implements DesSerFactory<RandomDoubleInterpolator> {

    @Override
    public void addDataForDeserialize(RandomDoubleInterpolator instance, Map<String, Object> data) {
        data.put("repeatableRandom", instance.repeatableRandom);
        data.put("min", instance.min);
        data.put("max", instance.max);
        data.put("changeScale", instance.changeScale);

        data.put("initialMin", instance.initialMin);
        data.put("initialMax", instance.initialMax);
        data.put("initialRepeatableRandom", instance.initialRepeatableRandom);
        data.put("initialChangeScale", instance.initialChangeScale);
    }

    @Override
    public RandomDoubleInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        RandomDoubleInterpolator currentValue = (RandomDoubleInterpolator) currentFieldValue;

        double min = data.get("min").asDouble();
        double max = data.get("max").asDouble();
        BigDecimal changeScale = new BigDecimal(data.get("changeScale").asText());

        double initialMin = data.get("initialMin").asDouble();
        double initialMax = data.get("initialMax").asDouble();
        BigDecimal initialChangeScale = new BigDecimal(data.get("initialChangeScale").asText());

        RandomDoubleInterpolator result = new RandomDoubleInterpolator(min, max, changeScale);

        result.initialMin = initialMin;
        result.initialMax = initialMax;
        result.initialChangeScale = initialChangeScale;

        result.repeatableRandom = ReflectionUtil.deserialize(data.get("repeatableRandom"), RepeatableRandom.class, currentValue.repeatableRandom, loadMetadata);
        return result;
    }

}
