package com.helospark.tactview.core.decoder;

import java.io.File;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class VideoMediaDataRequest {
    private String filePath;
    private VisualMediaMetadata metadata;

    private TimelinePosition start;
    private TimelineLength length;

    private boolean shouldRescale;
    private boolean useApproximatePosition;
    private int width;
    private int height;

    @Generated("SparkTools")
    private VideoMediaDataRequest(Builder builder) {
        this.filePath = builder.filePath;
        this.metadata = builder.metadata;
        this.start = builder.start;
        this.length = builder.length;
        this.shouldRescale = builder.shouldRescale;
        this.useApproximatePosition = builder.useApproximatePosition;
        this.width = builder.width;
        this.height = builder.height;
    }

    @Generated("SparkTools")
    public VideoMediaDataRequest() {
    }

    public File getFile() {
        return new File(filePath);
    }

    public String getFilePath() {
        return filePath;
    }

    public VisualMediaMetadata getMetadata() {
        return metadata;
    }

    public TimelinePosition getStart() {
        return start;
    }

    public TimelineLength getLength() {
        return length;
    }

    public boolean isShouldRescale() {
        return shouldRescale;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean useApproximatePosition() {
        return useApproximatePosition;
    }

    @Override
    public String toString() {
        return "VideoMediaDataRequest [filePath=" + filePath + ", metadata=" + metadata + ", start=" + start + ", length=" + length + ", shouldRescale=" + shouldRescale + ", useApproximatePosition="
                + useApproximatePosition + ", width=" + width + ", height=" + height + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String filePath;
        private VisualMediaMetadata metadata;
        private TimelinePosition start;
        private TimelineLength length;
        private boolean shouldRescale;
        private boolean useApproximatePosition;
        private int width;
        private int height;

        private Builder() {
        }

        public Builder withFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder withMetadata(VisualMediaMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder withStart(TimelinePosition start) {
            this.start = start;
            return this;
        }

        public Builder withLength(TimelineLength length) {
            this.length = length;
            return this;
        }

        public Builder withShouldRescale(boolean shouldRescale) {
            this.shouldRescale = shouldRescale;
            return this;
        }

        public Builder withUseApproximatePosition(boolean useApproximatePosition) {
            this.useApproximatePosition = useApproximatePosition;
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

        public VideoMediaDataRequest build() {
            return new VideoMediaDataRequest(this);
        }
    }

}
