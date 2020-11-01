package com.helospark.tactview.core.timeline.effect.interpolation.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElementFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElementFactory.GraphCreatorRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.StatelessEffectElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.VisualTimelineClipElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.message.GraphConnectionAddedMessage;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.timeline.message.ClipDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.EffectDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyAddedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class EffectGraphAccessor {
    private List<GraphElementFactory> graphElementFactories;
    private MessagingService messagingService;
    private EffectParametersRepository effectParametersRepository;

    public EffectGraphAccessor(List<GraphElementFactory> graphElementFactories, MessagingService messagingService,
            EffectParametersRepository effectParametersRepository) {
        this.graphElementFactories = graphElementFactories;
        this.messagingService = messagingService;
        this.effectParametersRepository = effectParametersRepository;
    }

    public GraphIndex addElementByUri(GraphProvider provider, String uri) {
        GraphElementFactory graphElementFactory = graphElementFactories.stream()
                .filter(factory -> factory.doesSupport(uri))
                .findFirst()
                .get();
        GraphElement createElement = graphElementFactory
                .createElement(new GraphCreatorRequest(provider, uri));

        // TODO: graph should itself has descriptors, instead of clip & effect, this should be refactored, so GraphElement provides descriptors
        // and EffectParameterRepository follows GraphElements as well, not just clips and effects
        if (createElement instanceof VisualTimelineClipElement) {
            VisualTimelineClipElement e = (VisualTimelineClipElement) createElement;
            messagingService.sendAsyncMessage(new ClipDescriptorsAdded(e.getClip().getId(), e.getClip().getDescriptors(), e.getClip()));
        } else if (createElement instanceof StatelessEffectElement) {
            StatelessEffectElement e = (StatelessEffectElement) createElement;
            messagingService.sendAsyncMessage(new EffectDescriptorsAdded(e.getEffect().getId(), e.getEffect().getValueProviders(), e.getEffect()));
        }
        sendKeyframeAddedMessage(provider);

        return provider.getEffectGraph().addNode(createElement);

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
