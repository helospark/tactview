package com.helospark.tactview.core.timeline;

public class AudioVideoFragment {
    private ClipFrameResult videoResult;
    private AudioFrameResult audioResult;

    public AudioVideoFragment(ClipFrameResult videoResult, AudioFrameResult audioResult) {
        this.videoResult = videoResult;
        this.audioResult = audioResult;
    }

    public ClipFrameResult getVideoResult() {
        return videoResult;
    }

    public AudioFrameResult getAudioResult() {
        return audioResult;
    }

}
