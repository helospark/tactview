package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class FileProviderFactory implements DesSerFactory<FileProvider> {

    @Override
    public void addDataForDeserialize(FileProvider instance, Map<String, Object> data) {
        data.put("extension", instance.extension);
        data.put("stringInterpolator", instance.stringInterpolator);
    }

    @Override
    public FileProvider deserialize(Map<String, Object> data) {
        return new FileProvider((String) data.get("extension"), (StringInterpolator) data.get("stringInterpolator"));
    }

}
