package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.SavedContentAddable;

public class FileProviderFactory implements DesSerFactory<FileProvider> {

    @Override
    public void addDataForDeserialize(FileProvider instance, Map<String, Object> data) {
        data.put("extension", instance.extension);
        data.put("stringInterpolator", instance.stringInterpolator);
    }

    @Override
    public FileProvider deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue) {
        return new FileProvider(data.get("extension").asText(), ReflectionUtil.deserialize(data.get("stringInterpolator"), StringInterpolator.class));
    }

}
