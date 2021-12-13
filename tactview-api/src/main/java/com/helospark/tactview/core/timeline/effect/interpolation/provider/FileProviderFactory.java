package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.AbstractKeyframeableEffectDesSerFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.util.ReflectionUtil;

public class FileProviderFactory extends AbstractKeyframeableEffectDesSerFactory<FileProvider> {

    @Override
    public void addDataForDeserializeInternal(FileProvider instance, Map<String, Object> data) {
        data.put("stringInterpolator", instance.stringInterpolator);
    }

    @Override
    public FileProvider deserializeInternal(JsonNode data, FileProvider currentFieldValue, LoadMetadata loadMetadata) {
        FileProvider currentValue = currentFieldValue;
        return new FileProvider(currentValue.getExtensions(), ReflectionUtil.deserialize(data.get("stringInterpolator"), StepStringInterpolator.class, currentFieldValue.stringInterpolator, loadMetadata));
    }

}
