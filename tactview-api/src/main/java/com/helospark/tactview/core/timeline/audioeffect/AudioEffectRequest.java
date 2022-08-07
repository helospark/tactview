package com.helospark.tactview.core.timeline.audioeffect;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;

public class AudioEffectRequest {
    private AudioFrameResult input;
    private TimelinePosition clipPosition;
    private TimelinePosition effectPosition;
    private EvaluationContext evaluationContext;

    @Generated("SparkTools")
    private AudioEffectRequest(Builder builder) {
        this.input = builder.input;
        this.clipPosition = builder.clipPosition;
        this.effectPosition = builder.effectPosition;
        this.evaluationContext = builder.evaluationContext;
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

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
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
        private EvaluationContext evaluationContext;

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

        public Builder withEvaluationContext(EvaluationContext evaluationContext) {
            this.evaluationContext = evaluationContext;
            return this;
        }

        public AudioEffectRequest build() {
            return new AudioEffectRequest(this);
        }
    }

}
