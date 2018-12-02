package com.helospark.tactview.core.timeline;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.image.ClipImage;

public class GetFrameRequest {
    private TimelinePosition position;
    private TimelinePosition relativePosition;
    private double scale;
    private int expectedWidth;
    private int expectedHeight;
    private boolean applyEffects;
    private Map<String, ClipImage> requestedClips;

    @Generated("SparkTools")
    private GetFrameRequest(Builder builder) {
        this.position = builder.position;
        this.relativePosition = builder.relativePosition;
        this.scale = builder.scale;
        this.expectedWidth = builder.expectedWidth;
        this.expectedHeight = builder.expectedHeight;
        this.applyEffects = builder.applyEffects;
        this.requestedClips = builder.requestedClips;
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

    public Map<String, ClipImage> getRequestedClips() {
        return requestedClips;
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
        private Map<String, ClipImage> requestedClips = Collections.emptyMap();

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

        public Builder withRequestedClips(Map<String, ClipImage> requestedClips) {
            this.requestedClips = requestedClips;
            return this;
        }

        public GetFrameRequest build() {
            return new GetFrameRequest(this);
        }
    }

}
