package com.helospark.tactview.core.util;

import java.util.Map;

public class RepeatableRandomFactory implements DesSerFactory<RepeatableRandom> {

    @Override
    public void addDataForDeserialize(RepeatableRandom random, Map<String, Object> data) {
        data.put("seed", random.seed);
    }

    @Override
    public RepeatableRandom deserialize(Map<String, Object> data) {
        Integer seed = (Integer) data.get("seed");
        return new RepeatableRandom(seed);
    }

}
