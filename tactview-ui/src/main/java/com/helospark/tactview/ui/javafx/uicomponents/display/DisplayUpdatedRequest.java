package com.helospark.tactview.ui.javafx.uicomponents.display;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.scene.image.Image;

public class DisplayUpdatedRequest {
    public TimelinePosition position;
    public Image image;

    @Generated("SparkTools")
    private DisplayUpdatedRequest(Builder builder) {
        this.position = builder.position;
        this.image = builder.image;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelinePosition position;
        private Image image;

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

        public DisplayUpdatedRequest build() {
            return new DisplayUpdatedRequest(this);
        }
    }
}
