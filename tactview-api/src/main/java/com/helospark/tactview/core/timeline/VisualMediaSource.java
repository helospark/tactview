package com.helospark.tactview.core.timeline;

import java.io.File;

import com.helospark.tactview.core.decoder.VisualMediaDecoder;

public class VisualMediaSource extends MediaSource {
    public VisualMediaDecoder decoder;

    public VisualMediaSource(File backingFile, VisualMediaDecoder decoder) {
        this.backingFile = backingFile.getAbsolutePath();
        this.decoder = decoder;
    }

    public VisualMediaSource(String backingFile, VisualMediaDecoder decoder) {
        this.backingFile = backingFile;
        this.decoder = decoder;
    }

    @Override
    public String toString() {
        return "VisualMediaSource [decoder=" + decoder + ", backingFile=" + backingFile + "]";
    }

}
