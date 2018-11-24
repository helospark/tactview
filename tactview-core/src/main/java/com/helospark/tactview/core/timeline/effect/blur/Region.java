package com.helospark.tactview.core.timeline.effect.blur;

import javax.annotation.Generated;

public class Region {
    int x, y;
    int width, height;

    @Generated("SparkTools")
    private Region(Builder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.width = builder.width;
        this.height = builder.height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private int x;
        private int y;
        private int width;
        private int height;

        private Builder() {
        }

        public Builder withx(int x) {
            this.x = x;
            return this;
        }

        public Builder withy(int y) {
            this.y = y;
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

        public Region build() {
            return new Region(this);
        }
    }
}
