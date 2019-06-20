package com.helospark.tactview.core.timeline.longprocess;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class LongProcessAudioPushRequest {
    private AudioFrameResult frame;
    private TimelinePosition position;

    @Generated("SparkTools")
    private LongProcessAudioPushRequest(Builder builder) {
        this.frame = builder.frame;
        this.position = builder.position;
    }

    public AudioFrameResult getFrame() {
        return frame;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private AudioFrameResult frame;
        private TimelinePosition position;

        private Builder() {
        }

        public Builder withFrame(AudioFrameResult frame) {
            this.frame = frame;
            return this;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public LongProcessAudioPushRequest build() {
            return new LongProcessAudioPushRequest(this);
        }
    }

}
