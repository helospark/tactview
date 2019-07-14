package com.helospark.tactview.core.util;

import com.helospark.tactview.core.DesSerFactory;

public interface SavedContentAddable<T> {

    public Class<? extends DesSerFactory<? extends T>> generateSerializableContent();

}
