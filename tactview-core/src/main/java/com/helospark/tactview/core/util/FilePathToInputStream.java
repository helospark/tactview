package com.helospark.tactview.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.helospark.lightdi.annotation.Component;

@Component
public class FilePathToInputStream {

    public InputStream fileNameToStream(String filename) {
        InputStream result = null;
        try {
            if (filename.startsWith("classpath:")) {
                Path path = Paths.get(this.getClass().getResource("/brushes/Oils-03.gbr").toURI());
                result = Files.newInputStream(path);
            } else if (filename.startsWith("file:")) {
                result = new FileInputStream(new File(filename.replaceFirst("filename:", "")));
            } else {
                result = new FileInputStream(new File(filename));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to open file " + filename);
        }
        if (result == null) {
            throw new IllegalArgumentException("Unable to open file " + filename);
        }
        return result;
    }
}
