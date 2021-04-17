package com.helospark.tactview.core.timeline;

import java.util.Optional;

import javax.annotation.Generated;

public class TimelineManagerFramesRequest {
    private double scale;
    private boolean needSound;
    private boolean needVideo;
    private boolean effectsEnabled;
    private boolean lowResolutionPreview;
    private Integer previewWidth;
    private Integer previewHeight;
    private TimelinePosition position;
    private Optional<TimelineLength> audioLength;
    private Optional<Integer> audioSampleRate;
    private Optional<Integer> audioBytesPerSample;
    private Optional<Integer> numberOfChannels;

    @Generated("SparkTools")
    private TimelineManagerFramesRequest(Builder builder) {
        this.scale = builder.scale;
        this.needSound = builder.needSound;
        this.needVideo = builder.needVideo;
        this.effectsEnabled = builder.effectsEnabled;
        this.lowResolutionPreview = builder.lowResolutionPreview;
        this.previewWidth = builder.previewWidth;
        this.previewHeight = builder.previewHeight;
        this.position = builder.position;
        this.audioLength = builder.audioLength;
        this.audioSampleRate = builder.audioSampleRate;
        this.audioBytesPerSample = builder.audioBytesPerSample;
        this.numberOfChannels = builder.numberOfChannels;
    }

    public boolean isNeedSound() {
        return needSound;
    }

    public boolean isNeedVideo() {
        return needVideo;
    }

    public boolean isEffectsEnabled() {
        return effectsEnabled;
    }

    public boolean isLowResolutionPreview() {
        return lowResolutionPreview;
    }

    public double getScale() {
        return scale;
    }

    public Integer getPreviewWidth() {
        return previewWidth;
    }

    public Integer getPreviewHeight() {
        return previewHeight;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public Optional<Integer> getAudioSampleRate() {
        return audioSampleRate;
    }

    public Optional<Integer> getAudioBytesPerSample() {
        return audioBytesPerSample;
    }

    public Optional<Integer> getNumberOfChannels() {
        return numberOfChannels;
    }

    public Optional<TimelineLength> getAudioLength() {
        return audioLength;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private double scale;
        private boolean needSound = true;
        private boolean needVideo = true;
        private boolean effectsEnabled = true;
        private boolean lowResolutionPreview = false;
        private Integer previewWidth;
        private Integer previewHeight;
        private TimelinePosition position;
        private Optional<TimelineLength> audioLength = Optional.empty();
        private Optional<Integer> audioSampleRate = Optional.empty();
        private Optional<Integer> audioBytesPerSample = Optional.empty();
        private Optional<Integer> numberOfChannels = Optional.empty();

        private Builder() {
        }

        public Builder withScale(double scale) {
            this.scale = scale;
            return this;
        }

        public Builder withNeedSound(boolean needSound) {
            this.needSound = needSound;
            return this;
        }

        public Builder withNeedVideo(boolean needVideo) {
            this.needVideo = needVideo;
            return this;
        }

        public Builder withEffectsEnabled(boolean effectsEnabled) {
            this.effectsEnabled = effectsEnabled;
            return this;
        }

        public Builder withLowResolutionPreview(boolean lowResolutionPreview) {
            this.lowResolutionPreview = lowResolutionPreview;
            return this;
        }

        public Builder withPreviewWidth(Integer previewWidth) {
            this.previewWidth = previewWidth;
            return this;
        }

        public Builder withPreviewHeight(Integer previewHeight) {
            this.previewHeight = previewHeight;
            return this;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withAudioLength(Optional<TimelineLength> audioLength) {
            this.audioLength = audioLength;
            return this;
        }

        public Builder withAudioSampleRate(Optional<Integer> audioSampleRate) {
            this.audioSampleRate = audioSampleRate;
            return this;
        }

        public Builder withAudioBytesPerSample(Optional<Integer> audioBytesPerSample) {
            this.audioBytesPerSample = audioBytesPerSample;
            return this;
        }

        public Builder withNumberOfChannels(Optional<Integer> numberOfChannels) {
            this.numberOfChannels = numberOfChannels;
            return this;
        }

        public TimelineManagerFramesRequest build() {
            return new TimelineManagerFramesRequest(this);
        }
    }
}
