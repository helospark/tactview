package com.helospark.tactview.core.timeline;

import javax.annotation.Generated;

public class GetFrameRequest {
    private TimelinePosition position;
    private double scale;
    private int expectedWidth;
    private int expectedHeight;

    @Generated("SparkTools")
    private GetFrameRequest(Builder builder) {
        this.position = builder.position;
        this.scale = builder.scale;
        this.expectedWidth = builder.expectedWidth;
        this.expectedHeight = builder.expectedHeight;
    }

    public TimelinePosition getPosition() {
        return position;
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

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelinePosition position;
        private double scale;
        private int expectedWidth;
        private int expectedHeight;

        private Builder() {
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
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

        public GetFrameRequest build() {
            return new GetFrameRequest(this);
        }
    }
}
