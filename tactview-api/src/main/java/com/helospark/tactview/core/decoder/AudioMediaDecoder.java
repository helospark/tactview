package com.helospark.tactview.core.decoder;

import java.io.File;

public interface AudioMediaDecoder {

    public AudioMediaMetadata readMetadata(File file);

    public MediaDataResponse readFrames(AudioMediaDataRequest request);
}
