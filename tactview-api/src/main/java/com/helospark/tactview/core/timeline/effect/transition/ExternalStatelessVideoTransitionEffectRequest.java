package com.helospark.tactview.core.timeline.effect.transition;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class ExternalStatelessVideoTransitionEffectRequest {
    private ReadOnlyClipImage firstFrame;
    private ReadOnlyClipImage secondFrame;
    private TimelinePosition globalPosition;
    private EvaluationContext evaluationContext;
    private double scale;

    @Generated("SparkTools")
    private ExternalStatelessVideoTransitionEffectRequest(Builder builder) {
        this.firstFrame = builder.firstFrame;
        this.secondFrame = builder.secondFrame;
        this.globalPosition = builder.globalPosition;
        this.evaluationContext = builder.evaluationContext;
        this.scale = builder.scale;
    }

    public ReadOnlyClipImage getFirstFrame() {
        return firstFrame;
    }

    public ReadOnlyClipImage getSecondFrame() {
        return secondFrame;
    }

    public TimelinePosition getGlobalPosition() {
        return globalPosition;
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }

    public double getScale() {
        return scale;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ReadOnlyClipImage firstFrame;
        private ReadOnlyClipImage secondFrame;
        private TimelinePosition globalPosition;
        private EvaluationContext evaluationContext;
        private double scale;

        private Builder() {
        }

        public Builder withFirstFrame(ReadOnlyClipImage firstFrame) {
            this.firstFrame = firstFrame;
            return this;
        }

        public Builder withSecondFrame(ReadOnlyClipImage secondFrame) {
            this.secondFrame = secondFrame;
            return this;
        }

        public Builder withGlobalPosition(TimelinePosition globalPosition) {
            this.globalPosition = globalPosition;
            return this;
        }

        public Builder withEvaluationContext(EvaluationContext evaluationContext) {
            this.evaluationContext = evaluationContext;
            return this;
        }

        public Builder withScale(double scale) {
            this.scale = scale;
            return this;
        }

        public ExternalStatelessVideoTransitionEffectRequest build() {
            return new ExternalStatelessVideoTransitionEffectRequest(this);
        }
    }

}
