package com.helospark.tactview.core.util;

import java.io.InputStream;
import java.nio.charset.Charset;

import com.helospark.lightdi.annotation.Component;

@Component
public class ClassPathResourceReaderImpl implements ClassPathResourceReader {

    /* (non-Javadoc)
     * @see com.helospark.tactview.core.util.ClassPathResourceReader#readClasspathFile(java.lang.String)
     */
    @Override
    public String readClasspathFile(String fileName) {
        try {
            return new String(readClasspathFileToByteArray(fileName), Charset.forName("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Cannot read file " + fileName, e);
        }
    }

    /* (non-Javadoc)
     * @see com.helospark.tactview.core.util.ClassPathResourceReader#readClasspathFileToByteArray(java.lang.String)
     */
    @Override
    public byte[] readClasspathFileToByteArray(String fileName) {
        try {
            InputStream stream = getClass().getResourceAsStream("/" + fileName);
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Cannot read file " + fileName, e);
        }
    }

}
