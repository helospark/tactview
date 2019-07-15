package com.helospark.tactview.core.util;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;

public class RepeatableRandomFactory implements DesSerFactory<RepeatableRandom> {

    @Override
    public void addDataForDeserialize(RepeatableRandom random, Map<String, Object> data) {
        data.put("seed", random.seed);
    }

    @Override
    public RepeatableRandom deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        Integer seed = data.get("seed").asInt();
        return new RepeatableRandom(seed);
    }

}
