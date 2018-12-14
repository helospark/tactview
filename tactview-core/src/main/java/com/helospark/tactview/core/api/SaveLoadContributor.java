package com.helospark.tactview.core.api;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public interface SaveLoadContributor {

    public void generateSavedContent(Map<String, Object> generatedContent);

    public void loadFrom(JsonNode tree, LoadMetadata metadata);

}
