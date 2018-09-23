package com.helospark.tactview.core.timeline;

import javax.annotation.Generated;

public class TimelineManagerFramesRequest {
    private Integer previewWidth;
    private Integer previewHeight;
    private Integer frameBufferSize;
    private TimelinePosition position;

    @Generated("SparkTools")
    private TimelineManagerFramesRequest(Builder builder) {
        this.previewWidth = builder.previewWidth;
        this.previewHeight = builder.previewHeight;
        this.frameBufferSize = builder.frameBufferSize;
        this.position = builder.position;
    }

    public Integer getPreviewWidth() {
        return previewWidth;
    }

    public Integer getPreviewHeight() {
        return previewHeight;
    }

    public Integer getFrameBufferSize() {
        return frameBufferSize;
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
        private Integer previewWidth;
        private Integer previewHeight;
        private Integer frameBufferSize;
        private TimelinePosition position;

        private Builder() {
        }

        public Builder withPreviewWidth(Integer previewWidth) {
            this.previewWidth = previewWidth;
            return this;
        }

        public Builder withPreviewHeight(Integer previewHeight) {
            this.previewHeight = previewHeight;
            return this;
        }

        public Builder withFrameBufferSize(Integer frameBufferSize) {
            this.frameBufferSize = frameBufferSize;
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
