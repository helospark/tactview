package com.helospark.tactview.core.timeline.subtimeline.loadhelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;

public class LoadData {
    public JsonNode tree;
    public LoadMetadata loadMetadata;

    public LoadData(JsonNode tree, LoadMetadata loadMetadata) {
        this.tree = tree;
        this.loadMetadata = loadMetadata;
    }

}
