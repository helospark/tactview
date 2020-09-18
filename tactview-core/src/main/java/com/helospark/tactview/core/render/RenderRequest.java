package com.helospark.tactview.core.render;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Generated;

import com.helospark.tactview.core.optionprovider.OptionProvider;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class RenderRequest {
    private final String renderId = UUID.randomUUID().toString();
    private final TimelinePosition startPosition;
    private final TimelinePosition endPosition;
    private final BigDecimal step;
    private final BigDecimal fps;
    private final int width;
    private final int height;
    private final BigDecimal upscale;
    private final String fileName;
    private final Map<String, OptionProvider<?>> options;
    private final Supplier<Boolean> isCancelledSupplier;
    private final Map<String, String> metadata;

    @Generated("SparkTools")
    private RenderRequest(Builder builder) {
        this.startPosition = builder.startPosition;
        this.endPosition = builder.endPosition;
        this.step = builder.step;
        this.fps = builder.fps;
        this.width = builder.width;
        this.height = builder.height;
        this.upscale = builder.upscale;
        this.fileName = builder.fileName;
        this.options = builder.options;
        this.metadata = builder.metadata;
        this.isCancelledSupplier = builder.isCancelledSupplier;
    }

    public String getRenderId() {
        return renderId;
    }

    public TimelinePosition getStartPosition() {
        return startPosition;
    }

    public TimelinePosition getEndPosition() {
        return endPosition;
    }

    public BigDecimal getStep() {
        return step;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getFileName() {
        return fileName;
    }

    public BigDecimal getFps() {
        return fps;
    }

    public BigDecimal getUpscale() {
        return upscale;
    }

    public Map<String, OptionProvider<?>> getOptions() {
        return options;
    }

    public Supplier<Boolean> getIsCancelledSupplier() {
        return isCancelledSupplier;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelinePosition startPosition;
        private TimelinePosition endPosition;
        private BigDecimal step;
        private BigDecimal fps;
        private int width;
        private int height;
        private BigDecimal upscale;
        private String fileName;
        private Map<String, OptionProvider<?>> options = Collections.emptyMap();
        private Supplier<Boolean> isCancelledSupplier;
        private Map<String, String> metadata = Collections.emptyMap();

        private Builder() {
        }

        public Builder withStartPosition(TimelinePosition startPosition) {
            this.startPosition = startPosition;
            return this;
        }

        public Builder withEndPosition(TimelinePosition endPosition) {
            this.endPosition = endPosition;
            return this;
        }

        public Builder withStep(BigDecimal step) {
            this.step = step;
            return this;
        }

        public Builder withFps(BigDecimal fps) {
            this.fps = fps;
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

        public Builder withUpscale(BigDecimal upscale) {
            this.upscale = upscale;
            return this;
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder withOptions(Map<String, OptionProvider<?>> options) {
            this.options = options;
            return this;
        }

        public Builder withMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder withIsCancelledSupplier(Supplier<Boolean> isCancelledSupplier) {
            this.isCancelledSupplier = isCancelledSupplier;
            return this;
        }

        public RenderRequest build() {
            return new RenderRequest(this);
        }
    }
}
