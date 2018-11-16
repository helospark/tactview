package com.helospark.tactview.core.decoder.gif;

import java.io.FileInputStream;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;
import com.madgag.gif.fmsware.GifDecoder;

@Component
public class GifFileReader {

    @Cacheable(cacheTimeInMilliseconds = 6000000, size = 50)
    public GifDecoder readFile(String path) {
        try (FileInputStream fis = new FileInputStream(path)) {
            GifDecoder gifDecoder = new GifDecoder();
            gifDecoder.read(fis);
            return gifDecoder;
        } catch (Exception e) {
            throw new RuntimeException("Unable to open file", e);
        }
    }
}
