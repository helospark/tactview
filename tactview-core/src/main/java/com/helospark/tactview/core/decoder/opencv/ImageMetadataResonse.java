package com.helospark.tactview.core.decoder.opencv;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class ImageMetadataResonse extends Structure implements Structure.ByValue {
    public int width;
    public int height;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("width", "height");
    }

}
