package com.helospark.tactview.core.repository;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveLoadContributor;

@Component
public class ProjectRepository implements SaveLoadContributor {
    @JsonIgnore
    private ObjectMapper objectMapper;

    private boolean isInitialized = false;
    private int width = 0;
    private int height = 0;
    private BigDecimal fps = BigDecimal.valueOf(24);
    private BigDecimal frameTime = BigDecimal.ONE.divide(fps, 20, RoundingMode.HALF_UP);

    public ProjectRepository(@Qualifier("getterIgnoringObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private ProjectRepository initialize(Builder builder) {
        this.isInitialized = builder.isInitialized;
        this.width = builder.width;
        this.height = builder.height;
        this.fps = builder.fps;
        this.frameTime = BigDecimal.ONE.divide(fps, 20, RoundingMode.HALF_UP);
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BigDecimal getFps() {
        return fps;
    }

    public BigDecimal getFrameTime() {
        return frameTime;
    }

    @Generated("SparkTools")
    public Builder initializer() {
        return new Builder();
    }

    @Generated("SparkTools")
    public final class Builder {
        private boolean isInitialized;
        private int width;
        private int height;
        private BigDecimal fps;

        private Builder() {
        }

        public Builder withIsInitialized(boolean isInitialized) {
            this.isInitialized = isInitialized;
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

        public Builder withFps(BigDecimal fps) {
            this.fps = fps;
            return this;
        }

        public ProjectRepository init() {
            return initialize(this);
        }
    }

    @Override
    public void generateSavedContent(Map<String, Object> generatedContent) {
        generatedContent.put("projectRepository", objectMapper.valueToTree(this));
    }

    @Override
    public void loadFrom(JsonNode tree, LoadMetadata metadata) {
        try {
            objectMapper.readerForUpdating(this).readValue(tree.get("projectRepository"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to load", e);
        }
    }

}
