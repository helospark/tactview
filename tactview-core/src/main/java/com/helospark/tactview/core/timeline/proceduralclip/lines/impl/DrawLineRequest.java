package com.helospark.tactview.core.timeline.proceduralclip.lines.impl;

import java.util.Collections;
import java.util.List;

import javax.annotation.Generated;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.image.ClipImage;

public class DrawLineRequest {
    private ClipImage result;
    private double progress;
    private List<Vector2D> pixels;
    private String brushFilePath;
    private int brushSize;
    private Color color;

    @Generated("SparkTools")
    private DrawLineRequest(Builder builder) {
        this.result = builder.result;
        this.progress = builder.progress;
        this.pixels = builder.pixels;
        this.brushFilePath = builder.brushFilePath;
        this.brushSize = builder.brushSize;
        this.color = builder.color;
    }

    public ClipImage getResult() {
        return result;
    }

    public double getProgress() {
        return progress;
    }

    public List<Vector2D> getPixels() {
        return pixels;
    }

    public String getBrushFilePath() {
        return brushFilePath;
    }

    public int getBrushSize() {
        return brushSize;
    }

    public Color getColor() {
        return color;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ClipImage result;
        private double progress = 1.0;
        private List<Vector2D> pixels = Collections.emptyList();
        private String brushFilePath = "classpath:/brushes/Sponge-02.gbr";
        private int brushSize = 40;
        private Color color = Color.of(0, 0, 0);

        private Builder() {
        }

        public Builder withResult(ClipImage result) {
            this.result = result;
            return this;
        }

        public Builder withProgress(double progress) {
            this.progress = progress;
            return this;
        }

        public Builder withPixels(List<Vector2D> pixels) {
            this.pixels = pixels;
            return this;
        }

        public Builder withBrushFilePath(String brushFilePath) {
            this.brushFilePath = brushFilePath;
            return this;
        }

        public Builder withBrushSize(int brushSize) {
            this.brushSize = brushSize;
            return this;
        }

        public Builder withColor(Color color) {
            this.color = color;
            return this;
        }

        public DrawLineRequest build() {
            return new DrawLineRequest(this);
        }
    }
}
