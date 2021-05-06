package com.helospark.tactview.core.render.domain;

import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class FFmpegRenderThreadResult {
    public AudioVideoFragment audioVideo;
    public TimelinePosition time;

    public FFmpegRenderThreadResult(AudioVideoFragment audioVideo, TimelinePosition time) {
        this.audioVideo = audioVideo;
        this.time = time;
    }

}
