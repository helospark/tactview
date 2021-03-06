package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;

public class TypingStringInterpolatorFactory implements DesSerFactory<TypingStringInterpolator> {

    @Override
    public void serializeInto(TypingStringInterpolator instance, Map<String, Object> data, SaveMetadata saveMetadata) {
        new StepStringInterpolatorFactory().serializeInto(instance.stepStringInterpolator, data, saveMetadata);
    }

    @Override
    public TypingStringInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        StepStringInterpolator stepInterpolator = new StepStringInterpolatorFactory().deserialize(data, currentFieldValue, loadMetadata);
        return new TypingStringInterpolator(stepInterpolator);
    }

}
