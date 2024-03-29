package com.helospark.tactview.core.timeline;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class GetFrameRequest {
    private TimelinePosition position;
    private TimelinePosition relativePosition;
    private double scale;
    private int expectedWidth;
    private int expectedHeight;
    private boolean applyEffects;
    private boolean useApproximatePosition;
    private boolean lowResolutionPreview;
    private boolean livePlayback;
    private Optional<Integer> applyEffectsLessThanEffectChannel;
    private Map<String, ReadOnlyClipImage> requestedVideoClips;
    private Map<String, AudioFrameResult> requestedAudioClips;
    private Map<String, ReadOnlyClipImage> requestedChannelClips;
    private EvaluationContext evaluationContext;

    @Generated("SparkTools")
    private GetFrameRequest(Builder builder) {
        this.position = builder.position;
        this.relativePosition = builder.relativePosition;
        this.scale = builder.scale;
        this.expectedWidth = builder.expectedWidth;
        this.expectedHeight = builder.expectedHeight;
        this.applyEffects = builder.applyEffects;
        this.useApproximatePosition = builder.useApproximatePosition;
        this.lowResolutionPreview = builder.lowResolutionPreview;
        this.livePlayback = builder.livePlayback;
        this.applyEffectsLessThanEffectChannel = builder.applyEffectsLessThanEffectChannel;
        this.requestedVideoClips = builder.requestedVideoClips;
        this.requestedChannelClips = builder.requestedChannelClips;
        this.evaluationContext = builder.evaluationContext;
        this.requestedAudioClips = builder.requestedAudioClips;
    }

    public TimelinePosition getGlobalPosition() {
        return position;
    }

    public TimelinePosition getRelativePosition() {
        return relativePosition;
    }

    public boolean isLowResolutionPreview() {
        return lowResolutionPreview;
    }

    public double getScale() {
        return scale;
    }

    public int getExpectedWidth() {
        return expectedWidth;
    }

    public int getExpectedHeight() {
        return expectedHeight;
    }

    public boolean isApplyEffects() {
        return applyEffects;
    }

    public boolean isLivePlayback() {
        return livePlayback;
    }

    public Map<String, ReadOnlyClipImage> getRequestedVideoClips() {
        return requestedVideoClips;
    }

    public Map<String, AudioFrameResult> getRequestedAudioClips() {
        return requestedAudioClips;
    }

    public Map<String, ReadOnlyClipImage> getRequestedChannelClips() {
        return requestedChannelClips;
    }

    public Optional<Integer> getApplyEffectsLessThanEffectChannel() {
        return applyEffectsLessThanEffectChannel;
    }

    public boolean useApproximatePosition() {
        return useApproximatePosition;
    }

    public TimelinePosition calculateRelativePositionFrom(IntervalAware intervalAware) {
        if (relativePosition != null) {
            return relativePosition;
        } else {
            return position.from(intervalAware.getInterval().getStartPosition());
        }
    }

    public void addDynamic(String id, String keyId, Object data) {
        if (evaluationContext != null) {
            evaluationContext.addDynamicVariable(id, keyId, data);
        }
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static Builder builderFrom(GetFrameRequest getFrameRequest) {
        return new Builder(getFrameRequest);
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelinePosition position;
        private TimelinePosition relativePosition;
        private double scale;
        private int expectedWidth;
        private int expectedHeight;
        private boolean applyEffects;
        private boolean useApproximatePosition;
        private boolean lowResolutionPreview;
        private boolean livePlayback;
        private Optional<Integer> applyEffectsLessThanEffectChannel = Optional.empty();
        private Map<String, ReadOnlyClipImage> requestedVideoClips = Collections.emptyMap();
        private Map<String, AudioFrameResult> requestedAudioClips = Collections.emptyMap();
        private Map<String, ReadOnlyClipImage> requestedChannelClips = Collections.emptyMap();
        private EvaluationContext evaluationContext;

        private Builder() {
        }

        private Builder(GetFrameRequest getFrameRequest) {
            this.position = getFrameRequest.position;
            this.relativePosition = getFrameRequest.relativePosition;
            this.scale = getFrameRequest.scale;
            this.expectedWidth = getFrameRequest.expectedWidth;
            this.expectedHeight = getFrameRequest.expectedHeight;
            this.applyEffects = getFrameRequest.applyEffects;
            this.useApproximatePosition = getFrameRequest.useApproximatePosition;
            this.lowResolutionPreview = getFrameRequest.lowResolutionPreview;
            this.livePlayback = getFrameRequest.livePlayback;
            this.applyEffectsLessThanEffectChannel = getFrameRequest.applyEffectsLessThanEffectChannel;
            this.requestedVideoClips = getFrameRequest.requestedVideoClips;
            this.requestedAudioClips = getFrameRequest.requestedAudioClips;
            this.requestedChannelClips = getFrameRequest.requestedChannelClips;
            this.evaluationContext = getFrameRequest.evaluationContext;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withRelativePosition(TimelinePosition relativePosition) {
            this.relativePosition = relativePosition;
            return this;
        }

        public Builder withScale(double scale) {
            this.scale = scale;
            return this;
        }

        public Builder withExpectedWidth(int expectedWidth) {
            this.expectedWidth = expectedWidth;
            return this;
        }

        public Builder withExpectedHeight(int expectedHeight) {
            this.expectedHeight = expectedHeight;
            return this;
        }

        public Builder withApplyEffects(boolean applyEffects) {
            this.applyEffects = applyEffects;
            return this;
        }

        public Builder withUseApproximatePosition(boolean useApproximatePosition) {
            this.useApproximatePosition = useApproximatePosition;
            return this;
        }

        public Builder withLowResolutionPreview(boolean lowResolutionPreview) {
            this.lowResolutionPreview = lowResolutionPreview;
            return this;
        }

        public Builder withLivePlayback(boolean livePlayback) {
            this.livePlayback = livePlayback;
            return this;
        }

        public Builder withApplyEffectsLessThanEffectChannel(Optional<Integer> applyEffectsLessThanEffectChannel) {
            this.applyEffectsLessThanEffectChannel = applyEffectsLessThanEffectChannel;
            return this;
        }

        public Builder withRequestedVideoClips(Map<String, ReadOnlyClipImage> requestedVideoClips) {
            this.requestedVideoClips = requestedVideoClips;
            return this;
        }

        public Builder withRequestedAudioClips(Map<String, AudioFrameResult> requestedAudioClips) {
            this.requestedAudioClips = requestedAudioClips;
            return this;
        }

        public Builder withRequestedChannelClips(Map<String, ReadOnlyClipImage> requestedChannelClips) {
            this.requestedChannelClips = requestedChannelClips;
            return this;
        }

        public Builder withEvaluationContext(EvaluationContext evaluationContext) {
            this.evaluationContext = evaluationContext;
            return this;
        }

        public GetFrameRequest build() {
            return new GetFrameRequest(this);
        }
    }

}
