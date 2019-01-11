package com.helospark.tactview.core.timeline.effect;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class StatelessEffectRequest {
    private ReadOnlyClipImage currentFrame;
    private TimelinePosition clipPosition;
    private TimelinePosition effectPosition;
    private double scale;
    private int effectChannel;
    private int canvasWidth;
    private int canvasHeight;
    private Map<String, ReadOnlyClipImage> requestedClips;
    private VisualTimelineClip currentTimelineClip;

    @Generated("SparkTools")
    private StatelessEffectRequest(Builder builder) {
        this.currentFrame = builder.currentFrame;
        this.clipPosition = builder.clipPosition;
        this.effectPosition = builder.effectPosition;
        this.scale = builder.scale;
        this.canvasWidth = builder.canvasWidth;
        this.canvasHeight = builder.canvasHeight;
        this.effectChannel = builder.effectChannel;
        this.requestedClips = builder.requestedClips;
        this.currentTimelineClip = builder.currentTimelineClip;
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

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public int getEffectChannel() {
        return effectChannel;
    }

    public Map<String, ReadOnlyClipImage> getRequestedClips() {
        return requestedClips;
    }

    /**
     * @deprecated Returning the entire clip is not a good idea, we should return a simplified view or bridge class here and/or move to the request base image request
     * that are satisfied by the render engine, however for know this will work, and when the requirements clearer and the renderengine is cleaned up a bit it will move there.
     */
    @Deprecated
    public VisualTimelineClip getCurrentTimelineClip() {
        return currentTimelineClip;
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
        private int canvasWidth;
        private int canvasHeight;
        private int effectChannel;
        private Map<String, ReadOnlyClipImage> requestedClips = Collections.emptyMap();
        private VisualTimelineClip currentTimelineClip;

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

        public Builder withCanvasWidth(int canvasWidth) {
            this.canvasWidth = canvasWidth;
            return this;
        }

        public Builder withCanvasHeight(int canvasHeight) {
            this.canvasHeight = canvasHeight;
            return this;
        }

        public Builder withEffectChannel(int effectChannel) {
            this.effectChannel = effectChannel;
            return this;
        }

        public Builder withRequestedClips(Map<String, ReadOnlyClipImage> requestedClips) {
            this.requestedClips = requestedClips;
            return this;
        }

        /**
         * @deprecated Returning the entire clip is not a good idea, we should return a simplified view or bridge class here and/or move to the request base image request
         * that are satisfied by the render engine, however for know this will work, and when the requirements clearer and the renderengine is cleaned up a bit it will move there.
         */
        @Deprecated
        public Builder withCurrentTimelineClip(VisualTimelineClip currentTimelineClip) {
            this.currentTimelineClip = currentTimelineClip;
            return this;
        }

        public StatelessEffectRequest build() {
            return new StatelessEffectRequest(this);
        }
    }

}
