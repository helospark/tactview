package com.helospark.tactview.core;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.util.SavedContentAddable;

public interface DesSerFactory<T> {

    public void addDataForDeserialize(T instance, Map<String, Object> data);

    public T deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata);
}
