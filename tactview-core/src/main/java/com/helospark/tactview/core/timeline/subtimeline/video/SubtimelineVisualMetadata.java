package com.helospark.tactview.core.timeline.subtimeline.video;

import javax.annotation.Generated;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.TimelineLength;

@JsonDeserialize(builder = SubtimelineVisualMetadata.Builder.class)
public class SubtimelineVisualMetadata extends VisualMediaMetadata {

    @Generated("SparkTools")
    private SubtimelineVisualMetadata(Builder builder) {
        this.length = builder.length;
        this.width = builder.width;
        this.height = builder.height;
        this.resizable = builder.resizable;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelineLength length;
        private int width;
        private int height;
        private boolean resizable;

        private Builder() {
        }

        public Builder withLength(TimelineLength length) {
            this.length = length;
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

        public Builder withResizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        public SubtimelineVisualMetadata build() {
            return new SubtimelineVisualMetadata(this);
        }
    }
}
