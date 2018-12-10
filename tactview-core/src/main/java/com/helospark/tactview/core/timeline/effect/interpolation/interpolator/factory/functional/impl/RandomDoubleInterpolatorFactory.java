package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.impl;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.RepeatableRandom;
import com.helospark.tactview.core.util.SavedContentAddable;

public class RandomDoubleInterpolatorFactory implements DesSerFactory<RandomDoubleInterpolator> {

    @Override
    public void addDataForDeserialize(RandomDoubleInterpolator instance, Map<String, Object> data) {
        data.put("min", instance.min);
        data.put("max", instance.max);
        data.put("repeatableRandom", instance.repeatableRandom);
        data.put("changeScale", instance.changeScale);
    }

    @Override
    public RandomDoubleInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue) {
        RandomDoubleInterpolator result = new RandomDoubleInterpolator(data.get("min").asInt(), data.get("max").asInt(), new BigDecimal(data.get("changeScale").asText()));
        result.repeatableRandom = ReflectionUtil.deserialize(data.get("repeatableRandom"), RepeatableRandom.class);
        return result;
    }

}
