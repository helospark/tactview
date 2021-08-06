package com.helospark.tactview.core.save;

import java.io.File;
import java.util.LinkedHashMap;
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

    public void save(SaveRequest saveRequest) {
        File rootDirectory = createRootDirectory();

        Map<String, Object> result = new LinkedHashMap<>();

        SaveMetadata saveMetadata = new SaveMetadata(saveRequest.isPackageAllContent());

        context.getListOfBeans(SaveLoadContributor.class)
                .forEach(a -> a.generateSavedContent(result, saveMetadata));

        createSavePackageFromResultt(saveRequest, rootDirectory, result, saveMetadata);

        deleteDirectory(rootDirectory);
    }

    @Override
    protected void loadAdditionalElements(JsonNode tree, LoadMetadata loadMetadata) {
        context.getListOfBeans(SaveLoadContributor.class)
                .forEach(a -> a.loadFrom(tree, loadMetadata));
    }
}
