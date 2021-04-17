package com.helospark.tactview.core.timeline;

import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.image.ClipImage;
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
        freeVideoResult();
        freeAudioResult();
    }

    private void freeVideoResult() {
        if (videoResult != null) {
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(videoResult.getBuffer());
        }
    }

    private void freeAudioResult() {
        if (audioResult != null) {
            for (int i = 0; i < audioResult.getChannels().size(); ++i) {
                GlobalMemoryManagerAccessor.memoryManager.returnBuffer(audioResult.getChannels().get(i));
            }
        }
    }

    public AudioVideoFragment butFreeAndReplaceVideoFrame(ClipImage scaledImage) {
        freeVideoResult();
        return new AudioVideoFragment(scaledImage, audioResult);
    }

    public AudioVideoFragment butFreeAndReplaceVideoFrame(AudioFrameResult newResult) {
        freeAudioResult();
        return new AudioVideoFragment(videoResult, newResult);
    }

}
