package com.helospark.tactview.core.render;

import java.math.BigDecimal;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class RenderRequest {
    private TimelinePosition startPosition;
    private TimelinePosition endPosition;
    private BigDecimal step;
    private int width;
    private int height;
    private double framerate;
    private String fileName;

    @Generated("SparkTools")
    private RenderRequest(Builder builder) {
        this.startPosition = builder.startPosition;
        this.endPosition = builder.endPosition;
        this.step = builder.step;
        this.width = builder.width;
        this.height = builder.height;
        this.framerate = builder.framerate;
        this.fileName = builder.fileName;
    }

    public TimelinePosition getStartPosition() {
        return startPosition;
    }

    public TimelinePosition getEndPosition() {
        return endPosition;
    }

    public BigDecimal getStep() {
        return step;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getFramerate() {
        return framerate;
    }

    public String getFileName() {
        return fileName;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelinePosition startPosition;
        private TimelinePosition endPosition;
        private BigDecimal step;
        private int width;
        private int height;
        private double framerate;
        private String fileName;

        private Builder() {
        }

        public Builder withStartPosition(TimelinePosition startPosition) {
            this.startPosition = startPosition;
            return this;
        }

        public Builder withEndPosition(TimelinePosition endPosition) {
            this.endPosition = endPosition;
            return this;
        }

        public Builder withStep(BigDecimal step) {
            this.step = step;
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

        public Builder withFramerate(double framerate) {
            this.framerate = framerate;
            return this;
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public RenderRequest build() {
            return new RenderRequest(this);
        }
    }
}
