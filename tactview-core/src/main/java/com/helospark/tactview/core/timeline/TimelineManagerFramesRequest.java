package com.helospark.tactview.core.timeline;

import javax.annotation.Generated;

public class TimelineManagerFramesRequest {
    private double scale;
    private boolean needSound;
    private boolean needVideo;
    private boolean effectsEnabled;
    private Integer previewWidth;
    private Integer previewHeight;
    private TimelinePosition position;

    @Generated("SparkTools")
    private TimelineManagerFramesRequest(Builder builder) {
        this.scale = builder.scale;
        this.needSound = builder.needSound;
        this.needVideo = builder.needVideo;
        this.effectsEnabled = builder.effectsEnabled;
        this.previewWidth = builder.previewWidth;
        this.previewHeight = builder.previewHeight;
        this.position = builder.position;
    }

    public boolean isNeedSound() {
        return needSound;
    }

    public boolean isNeedVideo() {
        return needVideo;
    }

    public boolean isEffectsEnabled() {
        return effectsEnabled;
    }

    public double getScale() {
        return scale;
    }

    public Integer getPreviewWidth() {
        return previewWidth;
    }

    public Integer getPreviewHeight() {
        return previewHeight;
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
        private double scale;
        private boolean needSound = true;
        private boolean needVideo = true;
        private boolean effectsEnabled = true;
        private Integer previewWidth;
        private Integer previewHeight;
        private TimelinePosition position;

        private Builder() {
        }

        public Builder withScale(double scale) {
            this.scale = scale;
            return this;
        }

        public Builder withNeedSound(boolean needSound) {
            this.needSound = needSound;
            return this;
        }

        public Builder withNeedVideo(boolean needVideo) {
            this.needVideo = needVideo;
            return this;
        }

        public Builder withEffectsEnabled(boolean effectsEnabled) {
            this.effectsEnabled = effectsEnabled;
            return this;
        }

        public Builder withPreviewWidth(Integer previewWidth) {
            this.previewWidth = previewWidth;
            return this;
        }

        public Builder withPreviewHeight(Integer previewHeight) {
            this.previewHeight = previewHeight;
            return this;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public TimelineManagerFramesRequest build() {
            return new TimelineManagerFramesRequest(this);
        }
    }
}
