package com.helospark.tactview.core.timeline.longprocess;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class LongProcessImagePushRequest {
    private ReadOnlyClipImage image;
    private TimelinePosition position;

    @Generated("SparkTools")
    private LongProcessImagePushRequest(Builder builder) {
        this.image = builder.image;
        this.position = builder.position;
    }

    public ReadOnlyClipImage getImage() {
        return image;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ReadOnlyClipImage image;
        private TimelinePosition position;

        private Builder() {
        }

        public Builder withImage(ReadOnlyClipImage image) {
            this.image = image;
            return this;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public LongProcessImagePushRequest build() {
            return new LongProcessImagePushRequest(this);
        }
    }
}
