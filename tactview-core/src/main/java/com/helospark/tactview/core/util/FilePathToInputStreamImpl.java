package com.helospark.tactview.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.helospark.lightdi.annotation.Component;

@Component
public class FilePathToInputStreamImpl implements FilePathToInputStream {

    @Override
    public InputStream fileNameToStream(String filename) {
        InputStream result = null;
        try {
            if (filename.startsWith("classpath:")) {
                String fixedFileName = filename.replaceFirst("classpath:", "");
                if (!fixedFileName.startsWith("/")) {
                    fixedFileName = "/" + fixedFileName;
                }
                Path path = Paths.get(this.getClass().getResource(fixedFileName).toURI());
                result = Files.newInputStream(path);
            } else if (filename.startsWith("file:")) {
                result = new FileInputStream(new File(filename.replaceFirst("filename:", "")));
            } else {
                result = new FileInputStream(new File(filename));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to open file " + filename, e);
        }
        if (result == null) {
            throw new IllegalArgumentException("Unable to open file " + filename);
        }
        return result;
    }
}
