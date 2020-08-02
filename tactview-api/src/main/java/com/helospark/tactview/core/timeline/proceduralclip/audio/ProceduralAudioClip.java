package com.helospark.tactview.core.timeline.proceduralclip.audio;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;

public abstract class ProceduralAudioClip extends AudibleTimelineClip {
    private String proceduralFactoryId;

    public ProceduralAudioClip(AudibleTimelineClip soundClip, CloneRequestMetadata cloneRequestMetadata) {
        super(soundClip, cloneRequestMetadata);
    }

    public ProceduralAudioClip(AudioMediaMetadata metadata, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(metadata, savedClip, loadMetadata);
    }

    public ProceduralAudioClip(TimelineInterval interval, AudioMediaMetadata mediaMetadata) {
        super(interval, mediaMetadata);
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent) {
        savedContent.put("proceduralFactoryId", proceduralFactoryId);
    }

    public String getProceduralFactoryId() {
        return proceduralFactoryId;
    }

    public void setProceduralFactoryId(String proceduralFactoryId) {
        this.proceduralFactoryId = proceduralFactoryId;
    }

}
