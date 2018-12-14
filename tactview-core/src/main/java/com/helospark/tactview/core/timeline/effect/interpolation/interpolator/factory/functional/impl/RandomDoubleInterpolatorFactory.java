package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.impl;

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
    }

    @Override
    public RandomDoubleInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        RandomDoubleInterpolator currentValue = (RandomDoubleInterpolator) currentFieldValue;
        RandomDoubleInterpolator result = new RandomDoubleInterpolator(currentValue.min, currentValue.max, currentValue.changeScale);
        result.repeatableRandom = ReflectionUtil.deserialize(data.get("repeatableRandom"), RepeatableRandom.class, currentValue.repeatableRandom, loadMetadata);
        return result;
    }

}
