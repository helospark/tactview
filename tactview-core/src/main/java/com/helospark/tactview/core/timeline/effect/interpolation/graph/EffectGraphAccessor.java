package com.helospark.tactview.core.timeline.effect.interpolation.graph;

import java.util.List;
import java.util.Map;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.CreateEffectRequest;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphIndex;

@Component
public class EffectGraphAccessor {
    private static final TimelineInterval INTERVAL = new TimelineInterval(TimelinePosition.ofZero(), TimelineLength.ofSeconds(1000000));
    private List<ClipFactory> clipFactories;
    private List<EffectFactory> effectFactories;

    public EffectGraphAccessor(List<ClipFactory> clipFactories, List<EffectFactory> effectFactories) {
        this.clipFactories = clipFactories;
        this.effectFactories = effectFactories;
    }

    public GraphIndex addProceduralClip(EffectGraph effectGraph, String proceduralClipId) {
        AddClipRequest request = AddClipRequest.builder()
                .withAddClipRequestMetadataKey(Map.of())
                .withPosition(TimelinePosition.ofZero())
                .withProceduralClipId(proceduralClipId)
                .build();
        VisualTimelineClip clip = (VisualTimelineClip) clipFactories.stream()
                .filter(a -> a.doesSupport(request))
                .map(a -> a.createClip(request))
                .findFirst()
                .get();
        clip.setInterval(INTERVAL);
        return effectGraph.addProceduralClip(clip);

    }

    public GraphIndex addClipFile(EffectGraph effectGraph, String fileName) {
        AddClipRequest request = AddClipRequest.builder()
                .withAddClipRequestMetadataKey(Map.of())
                .withPosition(TimelinePosition.ofZero())
                .withFilePath(fileName)
                .build();
        VisualTimelineClip clip = (VisualTimelineClip) clipFactories.stream()
                .filter(a -> a.doesSupport(request))
                .map(a -> a.createClip(request))
                .findFirst()
                .get();
        clip.setInterval(INTERVAL);
        return effectGraph.addProceduralClip(clip);
    }

    public GraphIndex addEffect(EffectGraph effectGraph, String replaceFirst) {
        CreateEffectRequest request = new CreateEffectRequest(TimelinePosition.ofZero(), replaceFirst, TimelineClipType.IMAGE, INTERVAL);
        EffectFactory factory = effectFactories
                .stream()
                .filter(effectFactory -> effectFactory.doesSupport(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No factory for " + request));
        StatelessVideoEffect result = (StatelessVideoEffect) factory.createEffect(request);
        result.setFactoryId(factory.getId());

        return effectGraph.addEffect(result);
    }

}
