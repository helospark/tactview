package com.helospark.tactview.ui.javafx.script;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.FilePathToInputStream;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class CachedFileContentReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedFileContentReader.class);
    private FilePathToInputStream filePathToInputStream;

    public CachedFileContentReader(FilePathToInputStream filePathToInputStream) {
        this.filePathToInputStream = filePathToInputStream;
    }

    @Cacheable(cacheTimeInMilliseconds = 60000, size = 100)
    public String readFile(String filename) {
        try {
            InputStream inputStream = filePathToInputStream.fileNameToStream(filename);
            String result = new String(inputStream.readAllBytes());

            return result;
        } catch (Exception e) {
            LOGGER.warn("Cannot read file " + filename, e);
            return null;
        }
    }

}
