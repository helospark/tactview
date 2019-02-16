package com.helospark.tactview.core.util;

import java.io.InputStream;
import java.nio.charset.Charset;

import com.helospark.lightdi.annotation.Component;

@Component
public class ClassPathResourceReader {

    public String readClasspathFile(String fileName) {
        try {
            InputStream stream = getClass().getResourceAsStream("/" + fileName);
            return new String(stream.readAllBytes(), Charset.forName("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Cannot read file " + fileName, e);
        }
    }

}
