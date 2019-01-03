package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;

public class TypingStringInterpolatorFactory implements DesSerFactory<TypingStringInterpolator> {

    @Override
    public void addDataForDeserialize(TypingStringInterpolator instance, Map<String, Object> data) {
        new StepStringInterpolatorFactory().addDataForDeserialize(instance.stepStringInterpolator, data);
    }

    @Override
    public TypingStringInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        StepStringInterpolator stepInterpolator = new StepStringInterpolatorFactory().deserialize(data, currentFieldValue, loadMetadata);
        return new TypingStringInterpolator(stepInterpolator);
    }

}
