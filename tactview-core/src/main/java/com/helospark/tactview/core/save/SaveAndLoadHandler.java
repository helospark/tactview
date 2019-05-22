package com.helospark.tactview.core.save;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.zeroturnaround.zip.ZipUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.StaticObjectMapper;

@Component
public class SaveAndLoadHandler {
    private static final String SAVEDATA_FILENAME = "savedata.json";
    private LightDiContext context;

    public SaveAndLoadHandler(LightDiContext context) {
        this.context = context;
    }

    public void save(SaveRequest saveRequest) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File rootDirectory = new File(tmpDir, "tactview_save_" + System.currentTimeMillis());
        if (rootDirectory.exists() || !rootDirectory.isDirectory()) {
            rootDirectory.delete();
        }
        rootDirectory.mkdirs();
        File saveDataJson = new File(rootDirectory, SAVEDATA_FILENAME);

        Map<String, Object> result = new LinkedHashMap<>();

        try (FileOutputStream outstream = new FileOutputStream(saveDataJson)) {
            context.getListOfBeans(SaveLoadContributor.class)
                    .forEach(a -> a.generateSavedContent(result));

            ObjectMapper mapper = StaticObjectMapper.objectMapper;
            String saveData = mapper.writeValueAsString(result);

            outstream.write(saveData.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String extension = saveRequest.getFileName().endsWith(".tvs") ? "" : ".tvs";
        File resultFile = new File(saveRequest.getFileName() + extension);

        resultFile.delete();

        ZipUtil.pack(rootDirectory, resultFile);
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

            LoadMetadata loadMetadata = new LoadMetadata(rootDirectory.getAbsolutePath());
            context.getListOfBeans(SaveLoadContributor.class)
                    .forEach(a -> a.loadFrom(tree, loadMetadata));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
