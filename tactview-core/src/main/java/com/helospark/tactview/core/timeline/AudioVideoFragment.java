package com.helospark.tactview.core.timeline;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class AudioVideoFragment {
    private ReadOnlyClipImage videoResult;
    private AudioFrameResult audioResult;

    public AudioVideoFragment(ReadOnlyClipImage videoResult, AudioFrameResult audioResult) {
        this.videoResult = videoResult;
        this.audioResult = audioResult;
    }

    public ReadOnlyClipImage getVideoResult() {
        return videoResult;
    }

    public AudioFrameResult getAudioResult() {
        return audioResult;
    }

}
