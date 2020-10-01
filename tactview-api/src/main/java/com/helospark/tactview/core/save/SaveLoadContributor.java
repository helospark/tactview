package com.helospark.tactview.core.save;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public interface SaveLoadContributor {

    public void generateSavedContent(Map<String, Object> generatedContent, SaveMetadata saveMetadata);

    public void loadFrom(JsonNode tree, LoadMetadata metadata);

}
