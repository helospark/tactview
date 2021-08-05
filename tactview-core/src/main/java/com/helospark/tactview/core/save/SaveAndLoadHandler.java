package com.helospark.tactview.core.save;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;

@Component
public class SaveAndLoadHandler extends AbstractSaveHandler {
    private static final String SAVEDATA_FILENAME = "savedata.json";

    public SaveAndLoadHandler(LightDiContext context) {
        super(SAVEDATA_FILENAME, context);
    }

    @Override
    protected void queryAdditionalDataToSave(Map<String, Object> result, SaveMetadata saveMetadata) {
        context.getListOfBeans(SaveLoadContributor.class)
                .forEach(a -> a.generateSavedContent(result, saveMetadata));
    }

    @Override
    protected void loadAdditionalElements(JsonNode tree, LoadMetadata loadMetadata) {
        context.getListOfBeans(SaveLoadContributor.class)
                .forEach(a -> a.loadFrom(tree, loadMetadata));
    }
}
