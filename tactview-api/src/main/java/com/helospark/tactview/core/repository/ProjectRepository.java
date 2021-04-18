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
import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveLoadContributor;
import com.helospark.tactview.core.save.SaveMetadata;

@Component
public class ProjectRepository implements SaveLoadContributor, ResettableBean {
    @JsonIgnore
    private ObjectMapper objectMapper;

    private boolean isVideoInitialized;
    private boolean isAudioInitialized;
    private int width;
    private int height;
    private int sampleRate;
    private int bytesPerSample;
    private int numberOfChannels;
    private BigDecimal fps;
    private BigDecimal frameTime;

    public ProjectRepository(@Qualifier("getterIgnoringObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        resetDefaults();
    }

    @Override
    public void resetDefaults() {
        isVideoInitialized = false;
        isAudioInitialized = false;
        width = 1920;
        height = 1080;
        sampleRate = 44100;
        bytesPerSample = 4;
        numberOfChannels = 2;
        fps = BigDecimal.valueOf(24);
        frameTime = BigDecimal.ONE.divide(fps, 100, RoundingMode.HALF_UP);
    }

    public void initializeVideo(int width, int height, BigDecimal fps) {
        this.isVideoInitialized = true;
        this.width = width;
        this.height = height;
        this.fps = fps;
        this.frameTime = BigDecimal.ONE.divide(fps, 100, RoundingMode.HALF_UP);
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
    public void generateSavedContent(Map<String, Object> generatedContent, SaveMetadata saveMetadata) {
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
