package com.helospark.tactview.core.util;

import java.util.Map;

public interface DesSerFactory<T> {

    public void addDataForDeserialize(T instance, Map<String, Object> data);

    public T deserialize(Map<String, Object> data);
}
