package com.helospark.tactview.core.timeline;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class GetFrameRequest {
    private TimelinePosition position;
    private TimelinePosition relativePosition;
    private double scale;
    private int expectedWidth;
    private int expectedHeight;
    private boolean applyEffects;
    private boolean useApproximatePosition;
    private Optional<Integer> applyEffectsLessThanEffectChannel;
    private Map<String, ReadOnlyClipImage> requestedClips;
    private Map<String, ReadOnlyClipImage> requestedChannelClips;

    @Generated("SparkTools")
    private GetFrameRequest(Builder builder) {
        this.position = builder.position;
        this.relativePosition = builder.relativePosition;
        this.scale = builder.scale;
        this.expectedWidth = builder.expectedWidth;
        this.expectedHeight = builder.expectedHeight;
        this.applyEffects = builder.applyEffects;
        this.useApproximatePosition = builder.useApproximatePosition;
        this.applyEffectsLessThanEffectChannel = builder.applyEffectsLessThanEffectChannel;
        this.requestedClips = builder.requestedClips;
        this.requestedChannelClips = builder.requestedChannelClips;
    }

    public TimelinePosition getGlobalPosition() {
        return position;
    }

    public TimelinePosition getRelativePosition() {
        return relativePosition;
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

    public Map<String, ReadOnlyClipImage> getRequestedClips() {
        return requestedClips;
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

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
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
        private Optional<Integer> applyEffectsLessThanEffectChannel = Optional.empty();
        private Map<String, ReadOnlyClipImage> requestedClips = Collections.emptyMap();
        private Map<String, ReadOnlyClipImage> requestedChannelClips = Collections.emptyMap();

        private Builder() {
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

        public Builder withApplyEffectsLessThanEffectChannel(Optional<Integer> applyEffectsLessThanEffectChannel) {
            this.applyEffectsLessThanEffectChannel = applyEffectsLessThanEffectChannel;
            return this;
        }

        public Builder withRequestedClips(Map<String, ReadOnlyClipImage> requestedClips) {
            this.requestedClips = requestedClips;
            return this;
        }

        public Builder withRequestedChannelClips(Map<String, ReadOnlyClipImage> requestedChannelClips) {
            this.requestedChannelClips = requestedChannelClips;
            return this;
        }

        public GetFrameRequest build() {
            return new GetFrameRequest(this);
        }
    }

}
