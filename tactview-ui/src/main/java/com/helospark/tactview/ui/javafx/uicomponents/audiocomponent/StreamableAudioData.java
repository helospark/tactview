package com.helospark.tactview.ui.javafx.uicomponents.audiocomponent;

import java.util.List;

import com.helospark.tactview.core.timeline.AudioVideoFragment;

public class StreamableAudioData {
    public List<AudioVideoFragment> originalData;
    public byte[] streamData;

    public StreamableAudioData(List<AudioVideoFragment> originalData, byte[] streamData) {
        this.originalData = originalData;
        this.streamData = streamData;
    }

    public void free() {
        originalData.stream()
                .forEach(a -> a.free());
    }

}
