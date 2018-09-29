package com.helospark.tactview.core.decoder;

import java.io.File;

public interface MediaDecoder {

    public VisualMediaMetadata readMetadata(File file);

    public MediaDataResponse readFrames(MediaDataRequest request);
}
