package com.helospark.tactview.core.timeline;

public class RequestFrameParameter {
    private TimelinePosition position;
    private int width;
    private int height;
    private boolean useApproximatePosition;
    private boolean lowResolutionPreview;
    private boolean isLivePlayback;
    private double scale;

    private RequestFrameParameter(Builder builder) {
        this.position = builder.position;
        this.width = builder.width;
        this.height = builder.height;
        this.useApproximatePosition = builder.useApproximatePosition;
        this.lowResolutionPreview = builder.lowResolutionPreview;
        this.scale = builder.scale;
        this.isLivePlayback = builder.isLivePlayback;
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

    public boolean isLivePlayback() {
        return isLivePlayback;
    }

    public boolean useApproximatePosition() {
        return useApproximatePosition;
    }

    public boolean isLowResolutionPreview() {
        return lowResolutionPreview;
    }

    public double getScale() {
        return scale;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private TimelinePosition position;
        private int width;
        private int height;
        private boolean useApproximatePosition;
        private boolean lowResolutionPreview;
        private boolean isLivePlayback;
        private double scale;

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

        public Builder withLivePlayback(boolean isLivePlayback) {
            this.isLivePlayback = isLivePlayback;
            return this;
        }

        public Builder withScale(double scale) {
            this.scale = scale;
            return this;
        }

        public RequestFrameParameter build() {
            return new RequestFrameParameter(this);
        }
    }

}