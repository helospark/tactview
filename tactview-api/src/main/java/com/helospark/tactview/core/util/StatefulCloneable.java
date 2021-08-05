package com.helospark.tactview.core.util;

import com.helospark.tactview.core.clone.CloneRequestMetadata;

public interface StatefulCloneable<T> {

    public T deepClone(CloneRequestMetadata cloneRequestMetadata);

}
