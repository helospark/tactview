package com.helospark.tactview.core.util;

import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;

@Component
public class ClasspathJsonParser {
    private ObjectMapper objectMapper;
    private ClassPathResourceReader classPathResourceReader;

    public ClasspathJsonParser(@Qualifier("simpleObjectMapper") ObjectMapper objectMapper, ClassPathResourceReader classPathResourceReader) {
        this.objectMapper = objectMapper;
        this.classPathResourceReader = classPathResourceReader;
    }

    public <T> T readClasspathFile(String fileName, TypeReference<T> reference) {
        try {
            String file = classPathResourceReader.readClasspathFile(fileName);
            return objectMapper.readValue(file, reference);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read file", e);
        }
    }
}
