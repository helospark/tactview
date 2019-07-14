package com.helospark.tactview.core.save;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.LoadMetadata;

public interface SaveLoadContributor {

    public void generateSavedContent(Map<String, Object> generatedContent);

    public void loadFrom(JsonNode tree, LoadMetadata metadata);

}
