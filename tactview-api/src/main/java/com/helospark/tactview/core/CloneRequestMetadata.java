package com.helospark.tactview.core;

public class CloneRequestMetadata {
    private boolean deepCloneId;

    public CloneRequestMetadata(boolean deepCloneId) {
        this.deepCloneId = deepCloneId;
    }

    public boolean isDeepCloneId() {
        return deepCloneId;
    }

    public static CloneRequestMetadata ofDefault() {
        return new CloneRequestMetadata(false);
    }

    public static CloneRequestMetadata fullCopy() {
        return new CloneRequestMetadata(true);
    }

}
