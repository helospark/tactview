package com.helospark.tactview.core.save;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.zeroturnaround.zip.ZipUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.StaticObjectMapper;

@Component
public class SaveAndLoadHandler extends AbstractSaveHandler {
    private static final String SAVEDATA_FILENAME = "savedata.json";
    private LightDiContext context;

    public SaveAndLoadHandler(LightDiContext context) {
        super(SAVEDATA_FILENAME);
        this.context = context;
    }

    @Override
    protected void queryDataToSave(Map<String, Object> result, SaveMetadata saveMetadata) {
        context.getListOfBeans(SaveLoadContributor.class)
                .forEach(a -> a.generateSavedContent(result, saveMetadata));
    }

    public void load(LoadRequest loadRequest) {
        try {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            File rootDirectory = new File(tmpDir, "tactview_save_" + System.currentTimeMillis());
            ZipUtil.unpack(new File(loadRequest.getFileName()), rootDirectory);

            ObjectMapper mapper = StaticObjectMapper.objectMapper;

            File fileName = new File(rootDirectory.getAbsolutePath(), SAVEDATA_FILENAME);

            String content = new String(Files.readAllBytes(Paths.get(fileName.getAbsolutePath())), StandardCharsets.UTF_8);

            JsonNode tree = mapper.readTree(content);

            LoadMetadata loadMetadata = new LoadMetadata(rootDirectory.getAbsolutePath(), mapper, context);
            context.getListOfBeans(SaveLoadContributor.class)
                    .forEach(a -> a.loadFrom(tree, loadMetadata));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
