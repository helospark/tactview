package com.helospark.tactview.core.util;

public interface SavedContentAddable<T> {

    public Class<? extends DesSerFactory<? extends T>> generateSerializableContent();

}
