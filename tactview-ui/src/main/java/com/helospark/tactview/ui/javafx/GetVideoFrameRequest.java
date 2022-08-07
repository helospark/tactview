package com.helospark.tactview.ui.javafx;

import java.util.Optional;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.PlaybackFrameAccessor.FrameSize;

public class GetVideoFrameRequest {
    public TimelinePosition position;
    public Optional<FrameSize> frameSize;
    public boolean livePlayback;
    public boolean isHalfEffect;

    public GetVideoFrameRequest(TimelinePosition position, Optional<FrameSize> frameSize, boolean livePlayback, boolean isHalfEffect) {
        this.position = position;
        this.frameSize = frameSize;
        this.livePlayback = livePlayback;
        this.isHalfEffect = isHalfEffect;
    }
}