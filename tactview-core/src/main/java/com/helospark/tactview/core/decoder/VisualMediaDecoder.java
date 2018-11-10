package com.helospark.tactview.core.decoder;

import java.io.File;

public interface VisualMediaDecoder {

    public VisualMediaMetadata readMetadata(File file);

    public MediaDataResponse readFrames(VideoMediaDataRequest request);
}
