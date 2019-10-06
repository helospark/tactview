package com.helospark.tactview.core.timeline;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.save.LoadMetadata;

@Component
public class ClipFactoryChain {
    private LightDiContext context;

    public ClipFactoryChain(LightDiContext context) {
        this.context = context;
    }

    public List<TimelineClip> createClips(AddClipRequest request) {
        List<ClipFactory> factories = findFactory(request);
        return factories
                .stream()
                .map(factory -> factory.createClip(request))
                .collect(Collectors.toList());
    }

    private List<ClipFactory> findFactory(AddClipRequest request) {
        return context.getListOfBeans(ClipFactory.class)
                .parallelStream()
                .filter(factory -> factory.doesSupport(request))
                .collect(Collectors.toList());
    }

    public TimelineClip restoreClip(JsonNode savedClip, LoadMetadata metadata) {
        String factoryId = savedClip.get("creatorFactoryId").asText();
        ClipFactory foundFactory = context.getListOfBeans(ClipFactory.class)
                .stream()
                .filter(factory -> factory.getId().equals(factoryId))
                .findFirst()
                .orElseThrow();
        TimelineClip result = foundFactory.restoreClip(savedClip, metadata);
        return result;
    }

}
