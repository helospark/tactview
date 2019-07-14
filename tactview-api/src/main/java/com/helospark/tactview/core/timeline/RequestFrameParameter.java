package com.helospark.tactview.core.timeline;

import javax.annotation.Generated;

public class RequestFrameParameter {
    private TimelinePosition position;
    private int width;
    private int height;
    private boolean useApproximatePosition;
    private boolean lowResolutionPreview;

    @Generated("SparkTools")
    private RequestFrameParameter(Builder builder) {
        this.position = builder.position;
        this.width = builder.width;
        this.height = builder.height;
        this.useApproximatePosition = builder.useApproximatePosition;
        this.lowResolutionPreview = builder.lowResolutionPreview;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean useApproximatePosition() {
        return useApproximatePosition;
    }

    public boolean isLowResolutionPreview() {
        return lowResolutionPreview;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelinePosition position;
        private int width;
        private int height;
        private boolean useApproximatePosition;
        private boolean lowResolutionPreview;

        private Builder() {
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder withHeight(int height) {
            this.height = height;
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

        public RequestFrameParameter build() {
            return new RequestFrameParameter(this);
        }
    }

}