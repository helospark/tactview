package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.ConditionalOnProperty;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.ClipFactoryChain;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.CreateEffectRequest;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElementFactory.GraphCategory;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.realtime.CameraOutputToV4L2LoopbackElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.realtime.camera.OpencvL4V2LoopbackImplementation;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.timeline.message.ClipDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyAddedMessage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralClipFactoryChainItem;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.conditional.ConditionalOnPlatform;
import com.helospark.tactview.core.util.conditional.TactviewPlatform;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Configuration
public class StandardGraphElementFactoryConfiguration {
    private static final TimelineInterval INTERVAL = new TimelineInterval(TimelinePosition.ofZero(), TimelineLength.ofSeconds(1000000));

    @Bean
    public GraphElementFactory outputGraphElementFactory() {
        return StandardGraphElementFactory.builder()
                .withId("output")
                .withName("Output")
                .withCategory(GraphCategory.OUTPUT)
                .withCreator(request -> new OutputElement())
                .withRestorer((node, metadata) -> new OutputElement(node, metadata))
                .build();
    }

    @Bean
    public GraphElementFactory fileClipGraphElementFactory(List<ClipFactory> clipFactories, ClipFactoryChain clipFactoryChain, MessagingService messagingService) {
        return StandardGraphElementFactory.builder()
                .withId("file")
                .withDoesSupport(uri -> uri.startsWith("file://"))
                .withName("File")
                .withCategory(GraphCategory.INPUT)
                .withNeedsInputParam(true)
                .withCreator(graphRequest -> {
                    AddClipRequest request = AddClipRequest.builder()
                            .withAddClipRequestMetadataKey(Map.of())
                            .withPosition(TimelinePosition.ofZero())
                            .withFilePath(graphRequest.uri.replaceFirst("file://", ""))
                            .build();

                    VisualTimelineClip clip = (VisualTimelineClip) clipFactoryChain.createClips(request).get(0);
                    clip.setInterval(INTERVAL);

                    messagingService.sendAsyncMessage(new ClipDescriptorsAdded(clip.getId(), clip.getDescriptors(), clip));

                    return new VisualTimelineClipElement(clip);
                })
                .withRestorer((node, metadata) -> {
                    node.get("creatorFactoryId").asText();
                    return new VisualTimelineClipElement(node, metadata, (VisualTimelineClip) clipFactoryChain.restoreClip(node.get("clip"), metadata));
                })
                .build();
    }

    @Bean
    public GraphElementFactory inputGraphElementFactory() {
        return StandardGraphElementFactory.builder()
                .withId("input")
                .withName("Input")
                .withCategory(GraphCategory.OUTPUT)
                .withCreator(request -> new InputElement())
                .withRestorer((node, metadata) -> new InputElement(node, metadata))
                .build();
    }

    @Bean
    @ConditionalOnPlatform(TactviewPlatform.LINUX)
    @ConditionalOnProperty(property = "tactview.realtime", havingValue = "true")
    public GraphElementFactory l4v2GraphElementFactory(OpencvL4V2LoopbackImplementation impl) {
        return StandardGraphElementFactory.builder()
                .withId("l4v2Graph")
                .withName("V4L2 loopback")
                .withCategory(GraphCategory.OUTPUT)
                .withCreator(request -> new CameraOutputToV4L2LoopbackElement(impl))
                .withRestorer((node, metadata) -> new CameraOutputToV4L2LoopbackElement(node, metadata, impl))
                .build();
    }

    @Bean
    public List<GraphElementFactory> effectGraphElementFactories(List<EffectFactory> effectFactories, EffectParametersRepository effectParametersRepository, MessagingService messagingService) {
        return effectFactories.stream()
                .map(factory -> {
                    return StandardGraphElementFactory.builder()
                            .withId("effect:" + factory.getId())
                            .withName(factory.getEffectName())
                            .withCategory(GraphCategory.EFFECT)
                            .withCreator(graphRequest -> {
                                CreateEffectRequest request = new CreateEffectRequest(TimelinePosition.ofZero(), factory.getEffectId(), TimelineClipType.IMAGE, INTERVAL);
                                StatelessVideoEffect effect = (StatelessVideoEffect) factory.createEffect(request);
                                effect.setFactoryId(factory.getId());
                                effect.setParentIntervalAware(() -> effectParametersRepository.findIntervalForValurProvider(graphRequest.provider.getId()).orElse(INTERVAL));

                                return new StatelessEffectElement(effect);
                            })
                            .withRestorer((node, metadata) -> new StatelessEffectElement(node, metadata, (StatelessVideoEffect) factory.restoreEffect(node.get("effect"), metadata)))
                            .build();
                }).collect(Collectors.toList());
    }

    @Bean
    public List<GraphElementFactory> proceduralClipElementFactories(List<ProceduralClipFactoryChainItem> effectFactories, EffectParametersRepository effectParametersRepository) {
        return effectFactories.stream()
                .map(factory -> {
                    return StandardGraphElementFactory.builder()
                            .withId("clip:" + factory.getId())
                            .withName(factory.getProceduralClipName())
                            .withCategory(GraphCategory.PROCEDURAL_CLIP)
                            .withCreator(graphRequest -> {
                                AddClipRequest request = AddClipRequest.builder()
                                        .withAddClipRequestMetadataKey(Map.of())
                                        .withPosition(TimelinePosition.ofZero())
                                        .withProceduralClipId(factory.getId())
                                        .build();
                                ProceduralVisualClip clip = factory.create(request);
                                clip.setInterval(INTERVAL);
                                return new VisualTimelineClipElement(clip);
                            })
                            .withRestorer((node, metadata) -> new VisualTimelineClipElement(node, metadata, factory.restoreClip(node.get("clip"), metadata)))
                            .build();
                }).collect(Collectors.toList());
    }

    private void sendKeyframeAddedMessage(EffectParametersRepository effectParametersRepository, MessagingService messagingService, GraphProvider provider) {
        Optional<TimelineInterval> interval = effectParametersRepository.findIntervalForValurProvider(provider.getId());
        Optional<String> containingElementId = effectParametersRepository.findContainingElementId(provider.getId());
        messagingService.sendAsyncMessage(new KeyframeSuccesfullyAddedMessage(provider.getId(), interval.get(), containingElementId.get()));
    }

}
