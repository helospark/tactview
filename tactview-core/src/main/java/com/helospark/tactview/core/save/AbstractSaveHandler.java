package com.helospark.tactview.core.save;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.deserializer.TimelinePositionMapDeserializer;
import com.helospark.tactview.core.util.ItemSerializer;
import com.helospark.tactview.core.util.SavedContentAddable;

public abstract class AbstractSaveHandler {
    private String saveFileName;

    public AbstractSaveHandler(String saveFileName) {
        this.saveFileName = saveFileName;
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
            queryDataToSave(result, saveMetadata);

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

    protected abstract void queryDataToSave(Map<String, Object> result, SaveMetadata saveMetadata);

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

}