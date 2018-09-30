package com.helospark.tactview.core.repository;

import java.math.BigDecimal;

import javax.annotation.Generated;

import com.helospark.lightdi.annotation.Component;

@Component
public class ProjectRepository {
    private boolean isInitialized = false;
    private int width = 0;
    private int height = 0;
    private BigDecimal fps = BigDecimal.ONE;

    private ProjectRepository initialize(Builder builder) {
        this.isInitialized = builder.isInitialized;
        this.width = builder.width;
        this.height = builder.height;
        this.fps = builder.fps;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BigDecimal getFps() {
        return fps;
    }

    @Generated("SparkTools")
    public Builder initializer() {
        return new Builder();
    }

    @Generated("SparkTools")
    public final class Builder {
        private boolean isInitialized;
        private int width;
        private int height;
        private BigDecimal fps;

        private Builder() {
        }

        public Builder withIsInitialized(boolean isInitialized) {
            this.isInitialized = isInitialized;
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

        public Builder withFps(BigDecimal fps) {
            this.fps = fps;
            return this;
        }

        public ProjectRepository init() {
            return initialize(this);
        }
    }

}
