package com.helospark.tactview.core.util;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;

@Component
public class ClasspathJsonParser {
    private ObjectMapper objectMapper;

    public ClasspathJsonParser(@Qualifier("simpleObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T readClasspathFile(String fileName, TypeReference<T> reference) {
        try {
            String file = readClasspathFile(fileName);
            return objectMapper.readValue(file, reference);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read file", e);
        }
    }

    public String readClasspathFile(String fileName) {
        try {
            URL fileUrl = this.getClass().getResource("/" + fileName);
            if (fileUrl == null) {
                throw new RuntimeException("Cannot find classpath file: " + fileName);
            }
            Path uri;
            uri = Paths.get(fileUrl.toURI());
            return new String(Files.readAllBytes(uri), Charset.forName("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Cannot read file", e);
        }
    }

}
