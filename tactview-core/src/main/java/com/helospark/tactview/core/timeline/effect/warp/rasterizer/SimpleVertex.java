package com.helospark.tactview.core.timeline.effect.warp.rasterizer;

import javax.annotation.Generated;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class SimpleVertex {
    Vector2D position;
    Color color;
    Vector2D textureCoordinates;
    ReadOnlyClipImage texture;

    @Generated("SparkTools")
    private SimpleVertex(Builder builder) {
        this.position = builder.position;
        this.color = builder.color;
        this.textureCoordinates = builder.textureCoordinates;
        this.texture = builder.texture;
    }

    public Vector2D getPosition() {
        return position;
    }

    public Color getColor() {
        return color;
    }

    public Vector2D getTextureCoordinates() {
        return textureCoordinates;
    }

    public ReadOnlyClipImage getTexture() {
        return texture;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private Vector2D position;
        private Color color;
        private Vector2D textureCoordinates;
        private ReadOnlyClipImage texture;

        private Builder() {
        }

        public Builder withPosition(Vector2D position) {
            this.position = position;
            return this;
        }

        public Builder withColor(Color color) {
            this.color = color;
            return this;
        }

        public Builder withTextureCoordinates(Vector2D textureCoordinates) {
            this.textureCoordinates = textureCoordinates;
            return this;
        }

        public Builder withTexture(ReadOnlyClipImage texture) {
            this.texture = texture;
            return this;
        }

        public SimpleVertex build() {
            return new SimpleVertex(this);
        }
    }
}
