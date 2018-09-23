package com.helospark.tactview.core.timeline;

import java.io.File;

import com.helospark.tactview.core.decoder.MediaDecoder;

public class VideoSource {
    public String backingFile;
    public MediaDecoder decoder;

    public VideoSource(File backingFile, MediaDecoder decoder) {
        this.backingFile = backingFile.getAbsolutePath();
        this.decoder = decoder;
    }

}
