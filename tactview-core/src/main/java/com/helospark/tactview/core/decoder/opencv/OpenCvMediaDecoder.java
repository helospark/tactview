package com.helospark.tactview.core.decoder.opencv;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvimagedecorder")
public interface OpenCvMediaDecoder extends Library {

    ImageMetadataResonse readMetadata(ImageMetadataRequest request);

    void readImage(ImageRequest request);
}
