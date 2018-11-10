package com.helospark.tactview.core.timeline;

import java.io.File;

import com.helospark.tactview.core.decoder.VisualMediaDecoder;

public class VisualMediaSource extends MediaSource {
    public VisualMediaDecoder decoder;

    public VisualMediaSource(File backingFile, VisualMediaDecoder decoder) {
        this.backingFile = backingFile.getAbsolutePath();
        this.decoder = decoder;
    }

}
