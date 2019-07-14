package com.helospark.tactview.core.repository;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.save.SaveLoadContributor;

@Component
public class ProjectRepository implements SaveLoadContributor {
    @JsonIgnore
    private ObjectMapper objectMapper;

    private boolean isVideoInitialized = false;
    private boolean isAudioInitialized = false;
    private int width = 1920;
    private int height = 1080;
    private int sampleRate = 44100;
    private int bytesPerSample = 4;
    private int numberOfChannels = 2;
    private BigDecimal fps = BigDecimal.valueOf(24);
    private BigDecimal frameTime = BigDecimal.ONE.divide(fps, 20, RoundingMode.HALF_UP);

    public ProjectRepository(@Qualifier("getterIgnoringObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void initializeVideo(int width, int height, BigDecimal fps) {
        this.isVideoInitialized = true;
        this.width = width;
        this.height = height;
        this.fps = fps;
        this.frameTime = BigDecimal.ONE.divide(fps, 20, RoundingMode.HALF_UP);
    }

    public void initializeAudio(int samleRate, int bytesPerSample, int numberOfChannels) {
        this.isAudioInitialized = true;
        this.sampleRate = samleRate;
        this.bytesPerSample = bytesPerSample;
        this.numberOfChannels = numberOfChannels;
    }

    public boolean isVideoInitialized() {
        return isVideoInitialized;
    }

    public boolean isAudioInitialized() {
        return isAudioInitialized;
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

    public int getSampleRate() {
        return sampleRate;
    }

    public int getBytesPerSample() {
        return bytesPerSample;
    }

    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    @Override
    public String toString() {
        return "ProjectRepository [objectMapper=" + objectMapper + ", isVideoInitialized=" + isVideoInitialized + ", isAudioInitialized=" + isAudioInitialized + ", width=" + width + ", height="
                + height + ", sampleRate=" + sampleRate + ", bytesPerSample=" + bytesPerSample + ", numberOfChannels=" + numberOfChannels + ", fps=" + fps + ", frameTime=" + frameTime + "]";
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
