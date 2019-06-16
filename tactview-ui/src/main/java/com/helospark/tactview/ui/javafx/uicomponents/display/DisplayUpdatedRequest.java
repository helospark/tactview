package com.helospark.tactview.ui.javafx.uicomponents.display;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class DisplayUpdatedRequest {
    public TimelinePosition position;
    public Image image;
    public GraphicsContext graphics;

    @Generated("SparkTools")
    private DisplayUpdatedRequest(Builder builder) {
        this.position = builder.position;
        this.image = builder.image;
        this.graphics = builder.graphics;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelinePosition position;
        private Image image;
        private GraphicsContext graphics;

        private Builder() {
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withImage(Image image) {
            this.image = image;
            return this;
        }

        public Builder withGraphics(GraphicsContext graphics) {
            this.graphics = graphics;
            return this;
        }

        public DisplayUpdatedRequest build() {
            return new DisplayUpdatedRequest(this);
        }
    }
}
