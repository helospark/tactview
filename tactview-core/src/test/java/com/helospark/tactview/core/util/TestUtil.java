package com.helospark.tactview.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtil {

    public static String readClasspathFile(String fileName) throws IOException, URISyntaxException {
        Path uri = Paths.get(TestUtil.class.getResource("/" + fileName).toURI());
        return new String(Files.readAllBytes(uri), Charset.forName("UTF-8"));
    }

    public static InputStream classpathFileToInputStream(String fileName) throws IOException, URISyntaxException {
        Path uri = Paths.get(TestUtil.class.getResource("/" + fileName).toURI());
        return Files.newInputStream(uri);
    }
}
