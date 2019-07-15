package com.helospark.tactview.core.util;

public interface ClassPathResourceReader {

    String readClasspathFile(String fileName);

    byte[] readClasspathFileToByteArray(String fileName);

}