package com.helospark.tactview.core.timeline;

import java.io.File;

import com.helospark.tactview.core.decoder.MediaDecoder;

public class MediaSource {
    public String backingFile;
    public MediaDecoder decoder;

    public MediaSource(File backingFile, MediaDecoder decoder) {
        this.backingFile = backingFile.getAbsolutePath();
        this.decoder = decoder;
    }

}
