package com.helospark.tactview.core.timeline.effect;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class StatelessEffectRequest {
    private ReadOnlyClipImage currentFrame;
    private TimelinePosition clipPosition;
    private TimelinePosition effectPosition;
    private double scale;
    private Map<String, ClipImage> requestedClips;

    @Generated("SparkTools")
    private StatelessEffectRequest(Builder builder) {
        this.currentFrame = builder.currentFrame;
        this.clipPosition = builder.clipPosition;
        this.effectPosition = builder.effectPosition;
        this.scale = builder.scale;
        this.requestedClips = builder.requestedClips;
    }

    public ReadOnlyClipImage getCurrentFrame() {
        return currentFrame;
    }

    public TimelinePosition getClipPosition() {
        return clipPosition;
    }

    public TimelinePosition getEffectPosition() {
        return effectPosition;
    }

    public double getScale() {
        return scale;
    }

    public Map<String, ClipImage> getRequestedClips() {
        return requestedClips;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ReadOnlyClipImage currentFrame;
        private TimelinePosition clipPosition;
        private TimelinePosition effectPosition;
        private double scale;
        private Map<String, ClipImage> requestedClips = Collections.emptyMap();

        private Builder() {
        }

        public Builder withCurrentFrame(ReadOnlyClipImage currentFrame) {
            this.currentFrame = currentFrame;
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

        public Builder withScale(double scale) {
            this.scale = scale;
            return this;
        }

        public Builder withRequestedClips(Map<String, ClipImage> requestedClips) {
            this.requestedClips = requestedClips;
            return this;
        }

        public StatelessEffectRequest build() {
            return new StatelessEffectRequest(this);
        }
    }

}
