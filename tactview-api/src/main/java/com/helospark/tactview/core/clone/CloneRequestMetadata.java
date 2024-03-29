package com.helospark.tactview.core.clone;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CloneRequestMetadata {
    private boolean deepCloneId;

    private Map<String, String> internalIdMapping = new HashMap<>();

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

    public String generateOrGetIdFromPrevious(String previousId) {
        if (internalIdMapping.containsKey(previousId)) {
            return internalIdMapping.get(previousId);
        } else {
            String newId = UUID.randomUUID().toString();
            internalIdMapping.put(previousId, newId);
            return newId;
        }
    }

    public String getPreviousId(String previousId) {
        String newId = internalIdMapping.get(previousId);
        if (newId == null) {
            throw new RuntimeException("Cannot map id " + previousId);
        }
        return newId;
    }

    public Map<String, String> getIdMapping() {
        return internalIdMapping;
    }

}
