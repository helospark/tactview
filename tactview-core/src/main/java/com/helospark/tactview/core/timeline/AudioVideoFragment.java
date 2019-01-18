package com.helospark.tactview.core.timeline;

import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
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

    public void free() {
        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(videoResult.getBuffer());
        for (int i = 0; i < audioResult.getChannels().size(); ++i) {
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(audioResult.getChannels().get(i));
        }
    }

}
