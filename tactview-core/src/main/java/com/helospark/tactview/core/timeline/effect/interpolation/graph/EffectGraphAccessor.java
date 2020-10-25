package com.helospark.tactview.core.timeline.effect.interpolation.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.message.GraphConnectionAddedMessage;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.timeline.message.ClipDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.EffectDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyAddedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class EffectGraphAccessor {
    private static final TimelineInterval INTERVAL = new TimelineInterval(TimelinePosition.ofZero(), TimelineLength.ofSeconds(1000000));
    private List<ClipFactory> clipFactories;
    private List<EffectFactory> effectFactories;
    private MessagingService messagingService;
    private EffectParametersRepository effectParametersRepository;

    public EffectGraphAccessor(List<ClipFactory> clipFactories, List<EffectFactory> effectFactories, MessagingService messagingService, EffectParametersRepository effectParametersRepository) {
        this.clipFactories = clipFactories;
        this.effectFactories = effectFactories;
        this.messagingService = messagingService;
        this.effectParametersRepository = effectParametersRepository;
    }

    public GraphIndex addProceduralClip(GraphProvider provider, String proceduralClipId) {
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
        messagingService.sendAsyncMessage(new ClipDescriptorsAdded(clip.getId(), clip.getDescriptors(), clip));
        sendKeyframeAddedMessage(provider);

        return provider.getEffectGraph().addProceduralClip(clip);

    }

    public GraphIndex addClipFile(GraphProvider provider, String fileName) {
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

        messagingService.sendAsyncMessage(new ClipDescriptorsAdded(clip.getId(), clip.getDescriptors(), clip));
        sendKeyframeAddedMessage(provider);

        return provider.getEffectGraph().addProceduralClip(clip);
    }

    public GraphIndex addEffect(GraphProvider provider, String replaceFirst) {
        CreateEffectRequest request = new CreateEffectRequest(TimelinePosition.ofZero(), replaceFirst, TimelineClipType.IMAGE, INTERVAL);
        EffectFactory factory = effectFactories
                .stream()
                .filter(effectFactory -> effectFactory.doesSupport(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No factory for " + request));
        StatelessVideoEffect result = (StatelessVideoEffect) factory.createEffect(request);
        result.setFactoryId(factory.getId());
        result.setParentIntervalAware(() -> effectParametersRepository.findIntervalForValurProvider(provider.getId()).orElse(INTERVAL));

        messagingService.sendAsyncMessage(new EffectDescriptorsAdded(result.getId(), result.getValueProviders(), result));
        sendKeyframeAddedMessage(provider);
        return provider.getEffectGraph().addEffect(result);
    }

    public GraphIndex addNode(GraphProvider provider, GraphElement graphElement) {
        GraphIndex result = provider.getEffectGraph().addNode(graphElement);
        sendKeyframeAddedMessage(provider);
        return result;
    }

    public void addConnection(GraphProvider provider, ConnectionIndex startIndex, ConnectionIndex endIndex) {
        Map<ConnectionIndex, List<ConnectionIndex>> originalConnections = provider.getEffectGraph().getConnections();
        List<ConnectionIndex> list = originalConnections.get(startIndex);

        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(endIndex);

        originalConnections.put(startIndex, list);
        Optional<TimelineInterval> interval = effectParametersRepository.findIntervalForValurProvider(provider.getId());
        Optional<String> containingElementId = effectParametersRepository.findContainingElementId(provider.getId());
        if (interval.isPresent() && containingElementId.isPresent()) {
            messagingService.sendAsyncMessage(new GraphConnectionAddedMessage(interval.get(), endIndex, endIndex));
        }
        sendKeyframeAddedMessage(provider);
    }

    private void sendKeyframeAddedMessage(GraphProvider provider) {
        Optional<TimelineInterval> interval = effectParametersRepository.findIntervalForValurProvider(provider.getId());
        Optional<String> containingElementId = effectParametersRepository.findContainingElementId(provider.getId());
        messagingService.sendAsyncMessage(new KeyframeSuccesfullyAddedMessage(provider.getId(), interval.get(), containingElementId.get()));
    }

    public void removeConnection(GraphProvider provider, ConnectionIndex startIndex, ConnectionIndex endIndex) {
        Map<ConnectionIndex, List<ConnectionIndex>> originalConnections = provider.getEffectGraph().getConnections();

        List<ConnectionIndex> list = originalConnections.get(startIndex);
        if (list != null) {
            list.remove(endIndex);
        }

        sendKeyframeAddedMessage(provider);
    }

    public void removeElementById(GraphProvider provider, GraphIndex graphAddedNode) {
        provider.getEffectGraph().removeElementById(graphAddedNode);
    }

}
