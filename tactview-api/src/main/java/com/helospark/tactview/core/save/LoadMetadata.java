package com.helospark.tactview.core.save;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.LightDiContext;

public class LoadMetadata {
    private String fileLocation;
    private ObjectMapper objectMapperUsed;
    // See GraphProviderFactory for usage, either save and load is separated to different classes, or DiContext is passed into non-bean classes.
    // This seems like the least bad option
    private LightDiContext lightDiContext;

    public LoadMetadata(String fileLocation, ObjectMapper mapper, LightDiContext lightDiContext) {
        this.fileLocation = fileLocation;
        this.objectMapperUsed = mapper;
        this.lightDiContext = lightDiContext;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public ObjectMapper getObjectMapperUsed() {
        return objectMapperUsed;
    }

    public LightDiContext getLightDiContext() {
        return lightDiContext;
    }

    // TODO: It's not really ideal in here, but avoids lots of copyPaste
    public File resolveFilePath(String fileName) {
        File file;
        if (fileName.startsWith(SaveMetadata.LOCALLY_SAVED_SOURCE_PREFIX)) {
            file = new File(getFileLocation(), fileName.substring(SaveMetadata.LOCALLY_SAVED_SOURCE_PREFIX.length()));
        } else {
            file = new File(fileName);
        }
        return file;
    }
}
