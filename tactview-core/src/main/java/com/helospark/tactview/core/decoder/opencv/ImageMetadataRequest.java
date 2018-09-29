package com.helospark.tactview.core.decoder.opencv;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class ImageMetadataRequest extends Structure implements Structure.ByReference {
    public String path;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("path");
    }

}
