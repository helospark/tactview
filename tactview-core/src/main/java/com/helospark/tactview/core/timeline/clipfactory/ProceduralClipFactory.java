package com.helospark.tactview.core.timeline.clipfactory;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.api.LoadMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralClipFactoryChainItem;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;

@Component
public class ProceduralClipFactory implements ClipFactory {
    private List<ProceduralClipFactoryChainItem> factories;

    public ProceduralClipFactory(List<ProceduralClipFactoryChainItem> factories) {
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
    public VisualMediaMetadata readMetadata(AddClipRequest request) {
        return ImageMetadata.builder()
                .withWidth(1920)
                .withHeight(1080)
                .withLength(TimelineLength.ofMillis(5000))
                .build();
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        ProceduralClipFactoryChainItem proceduralFactory = factories.stream()
                .filter(factory -> factory.doesSupport(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nothing can handle " + request));
        ProceduralVisualClip proceduralTimelineClip = proceduralFactory
                .create(request);
        proceduralTimelineClip.setProceduralFactoryId(proceduralFactory.getId());
        proceduralTimelineClip.setCreatorFactoryId(getId());
        return proceduralTimelineClip;
    }

    @Override
    public String getId() {
        return "proceduralClipFactory";
    }

    @Override
    public TimelineClip restoreClip(JsonNode savedClip, LoadMetadata metadata) {
        String proceduralFactoryId = savedClip.get("proceduralFactoryId").asText();
        ProceduralClipFactoryChainItem proceduralFactory = factories.stream()
                .filter(factory -> factory.getProceduralClipId().equals(proceduralFactoryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nothing can handle " + proceduralFactoryId));
        ProceduralVisualClip result = proceduralFactory.restoreClip(savedClip, metadata);
        result.setProceduralFactoryId(proceduralFactoryId);
        return result;
    }

}
