package com.helospark.tactview.core.timeline;

import java.io.File;

import com.helospark.tactview.core.decoder.AudioMediaDecoder;

public class AudioMediaSource extends MediaSource {
    public AudioMediaDecoder decoder;

    public AudioMediaSource(File backingFile, AudioMediaDecoder decoder) {
        this.backingFile = backingFile.getAbsolutePath();
        this.decoder = decoder;
    }

}
