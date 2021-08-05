package com.helospark.tactview.core.save;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.deserializer.TimelinePositionMapDeserializer;
import com.helospark.tactview.core.util.ItemSerializer;
import com.helospark.tactview.core.util.SavedContentAddable;
import com.helospark.tactview.core.util.StaticObjectMapper;

public abstract class AbstractSaveHandler {
    private String saveFileName;
    protected LightDiContext context;

    public AbstractSaveHandler(String saveFileName, LightDiContext context) {
        this.saveFileName = saveFileName;
        this.context = context;
    }

    public void save(SaveRequest saveRequest) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File rootDirectory = new File(tmpDir, "tactview_save_" + System.currentTimeMillis());
        if (rootDirectory.exists() || rootDirectory.isDirectory()) {
            deleteDirectory(rootDirectory);
        }
        rootDirectory.mkdirs();
        File saveDataJson = new File(rootDirectory, saveFileName);

        Map<String, Object> result = new LinkedHashMap<>();

        SaveMetadata saveMetadata = new SaveMetadata(saveRequest.isPackageAllContent());

        try (FileOutputStream outstream = new FileOutputStream(saveDataJson)) {
            queryAdditionalDataToSave(result, saveMetadata);

            ObjectMapper mapper = createObjectMapper(saveMetadata);
            String saveData = mapper.writeValueAsString(result);

            outstream.write(saveData.getBytes());

            // copy files
            for (var entry : saveMetadata.getFilesToCopy().entrySet()) {
                File toFile = new File(rootDirectory, entry.getKey());
                File fromFile = new File(entry.getValue());

                toFile.getParentFile().mkdirs();

                if (fromFile.exists()) {
                    FileUtils.copyFile(fromFile, toFile);
                }
            }

            // Copy data
            for (var entry : saveMetadata.getDataToCopy().entrySet()) {
                File toFile = new File(rootDirectory, entry.getKey());
                byte[] data = entry.getValue();

                toFile.getParentFile().mkdirs();

                try (var fos = new FileOutputStream(toFile)) {
                    fos.write(data);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File resultFile = new File(saveRequest.getFileName());

        resultFile.delete();

        ZipUtil.pack(rootDirectory, resultFile);

        deleteDirectory(rootDirectory);
    }

    protected abstract void queryAdditionalDataToSave(Map<String, Object> result, SaveMetadata saveMetadata);

    protected ObjectMapper createObjectMapper(SaveMetadata saveMetadata) {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(SavedContentAddable.class, new ItemSerializer(saveMetadata));
        module.addKeyDeserializer(TimelinePosition.class, new TimelinePositionMapDeserializer());
        objectMapper.registerModule(module);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        return objectMapper;
    }

    protected void deleteDirectory(File rootDirectory) {
        try {
            FileUtils.deleteDirectory(rootDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load(LoadRequest loadRequest) {
        try {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            File rootDirectory = new File(tmpDir, "tactview_save_" + System.currentTimeMillis());
            ZipUtil.unpack(new File(loadRequest.getFileName()), rootDirectory);

            ObjectMapper mapper = StaticObjectMapper.objectMapper;

            File fileName = new File(rootDirectory.getAbsolutePath(), saveFileName);

            String content = new String(Files.readAllBytes(Paths.get(fileName.getAbsolutePath())), StandardCharsets.UTF_8);

            JsonNode tree = mapper.readTree(content);

            LoadMetadata loadMetadata = new LoadMetadata(rootDirectory.getAbsolutePath(), mapper, context);
            loadAdditionalElements(tree, loadMetadata);

            rootDirectory.deleteOnExit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void loadAdditionalElements(JsonNode tree, LoadMetadata loadMetadata);
}