package com.helospark.tactview.core.decoder.opencv;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class ImageRequest extends Structure implements Structure.ByReference {
    public String path;
    public int width;
    public int height;
    public ByteBuffer data;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("path", "width", "height", "data");
    }
}
