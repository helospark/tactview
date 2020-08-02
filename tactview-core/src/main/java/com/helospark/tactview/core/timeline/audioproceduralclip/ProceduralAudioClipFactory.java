package com.helospark.tactview.core.timeline.audioproceduralclip;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.proceduralclip.audio.ProceduralAudioClip;
import com.helospark.tactview.core.timeline.proceduralclip.audio.ProceduralAudioClipFactoryChainItem;

@Component
public class ProceduralAudioClipFactory implements ClipFactory {
    private final List<ProceduralAudioClipFactoryChainItem> factories;

    public ProceduralAudioClipFactory(List<ProceduralAudioClipFactoryChainItem> factories) {
        this.factories = factories;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return factories.stream()
                .filter(factory -> factory.doesSupport(request))
                .findFirst()
                .isPresent();
    }

    @Override
    public AudioMediaMetadata readMetadata(AddClipRequest request) {
        return AudioMediaMetadata.builder()
                .withBytesPerSample(2)
                .withChannels(2)
                .withLength(TimelineLength.ofSeconds(30.0))
                .withSampleRate(44100)
                .withBitRate(1000)
                .build();
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        ProceduralAudioClipFactoryChainItem proceduralFactory = factories.stream()
                .filter(factory -> factory.doesSupport(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nothing can handle " + request));
        ProceduralAudioClip proceduralTimelineClip = proceduralFactory.create(request);
        proceduralTimelineClip.setProceduralFactoryId(proceduralFactory.getId());
        proceduralTimelineClip.setCreatorFactoryId(getId());
        return proceduralTimelineClip;
    }

    @Override
    public String getId() {
        return "proceduralAudioClipFactory";
    }

    @Override
    public TimelineClip restoreClip(JsonNode savedClip, LoadMetadata metadata) {
        String proceduralFactoryId = savedClip.get("proceduralFactoryId").asText();
        ProceduralAudioClipFactoryChainItem proceduralFactory = factories.stream()
                .filter(factory -> factory.getProceduralClipId().equals(proceduralFactoryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nothing can handle " + proceduralFactoryId));
        ProceduralAudioClip result = proceduralFactory.restoreClip(savedClip, metadata);
        result.setProceduralFactoryId(proceduralFactoryId);
        return result;
    }

}
