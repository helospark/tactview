package com.helospark.tactview.core.timeline.audioeffect;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class AudioEffectRequest {
    private AudioFrameResult input;
    private TimelinePosition clipPosition;
    private TimelinePosition effectPosition;

    @Generated("SparkTools")
    private AudioEffectRequest(Builder builder) {
        this.input = builder.input;
        this.clipPosition = builder.clipPosition;
        this.effectPosition = builder.effectPosition;
    }

    public TimelinePosition getClipPosition() {
        return clipPosition;
    }

    public AudioFrameResult getInput() {
        return input;
    }

    public TimelinePosition getEffectPosition() {
        return effectPosition;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private AudioFrameResult input;
        private TimelinePosition clipPosition;
        private TimelinePosition effectPosition;

        private Builder() {
        }

        public Builder withInput(AudioFrameResult input) {
            this.input = input;
            return this;
        }

        public Builder withClipPosition(TimelinePosition clipPosition) {
            this.clipPosition = clipPosition;
            return this;
        }

        public Builder withEffectPosition(TimelinePosition effectPosition) {
            this.effectPosition = effectPosition;
            return this;
        }

        public AudioEffectRequest build() {
            return new AudioEffectRequest(this);
        }
    }

}
