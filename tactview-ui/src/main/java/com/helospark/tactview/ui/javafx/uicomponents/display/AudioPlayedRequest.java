package com.helospark.tactview.ui.javafx.uicomponents.display;

import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class AudioPlayedRequest {
    private TimelinePosition position;
    private AudioFrameResult audioFrameResult;

    public AudioPlayedRequest(TimelinePosition position, AudioFrameResult audioFrameResult) {
        this.position = position;
        this.audioFrameResult = audioFrameResult;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public AudioFrameResult getAudioFrameResult() {
        return audioFrameResult;
    }

}
