package com.helospark.tactview.core.util;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;

public interface DesSerFactory<T> {

    public void serializeInto(T instance, Map<String, Object> data, SaveMetadata saveMetadata);

    public T deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata);
}
