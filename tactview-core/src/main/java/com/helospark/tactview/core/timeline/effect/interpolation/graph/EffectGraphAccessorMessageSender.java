package com.helospark.tactview.core.timeline.effect.interpolation.graph;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.StatelessEffectElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.VisualTimelineClipElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.timeline.message.ClipDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.EffectDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyAddedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class EffectGraphAccessorMessageSender {
    private MessagingService messagingService;
    private EffectParametersRepository effectParametersRepository;

    public EffectGraphAccessorMessageSender(MessagingService messagingService, EffectParametersRepository effectParametersRepository) {
        this.messagingService = messagingService;
        this.effectParametersRepository = effectParametersRepository;
    }

    public void sendProviderMessageFor(GraphProvider effectGraphProvider) {
        for (var entry : effectGraphProvider.getEffectGraph().getGraphElements().entrySet()) {
            sendProviderMessages(effectGraphProvider, entry.getValue());
        }
    }

    public void sendProviderMessages(GraphProvider provider, GraphElement createElement) {
        // TODO: graph should itself has descriptors, instead of clip & effect, this should be refactored, so GraphElement provides descriptors
        // and EffectParameterRepository follows GraphElements as well, not just clips and effects
        if (createElement instanceof VisualTimelineClipElement) {
            VisualTimelineClipElement e = (VisualTimelineClipElement) createElement;
            messagingService.sendAsyncMessage(new ClipDescriptorsAdded(e.getClip().getId(), e.getClip().getDescriptors(), e.getClip()));
        } else if (createElement instanceof StatelessEffectElement) {
            StatelessEffectElement e = (StatelessEffectElement) createElement;
            messagingService.sendAsyncMessage(new EffectDescriptorsAdded(e.getEffect().getId(), e.getEffect().getValueProviders(), e.getEffect()));
        }
    }

    public void sendKeyframeAddedMessage(GraphProvider provider) {
        Optional<TimelineInterval> interval = effectParametersRepository.findIntervalForValurProvider(provider.getId());
        Optional<String> containingElementId = effectParametersRepository.findContainingElementId(provider.getId());
        messagingService.sendAsyncMessage(new KeyframeSuccesfullyAddedMessage(provider.getId(), interval.get(), containingElementId.get()));
    }
}
