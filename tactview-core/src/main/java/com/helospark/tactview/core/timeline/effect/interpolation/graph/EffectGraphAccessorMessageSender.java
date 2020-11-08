package com.helospark.tactview.core.timeline.effect.interpolation.graph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.timeline.message.GraphComponentDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.GraphNodeAddedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyAddedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class EffectGraphAccessorMessageSender {
    private MessagingService messagingService;

    public EffectGraphAccessorMessageSender(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public void sendProviderMessageFor(GraphProvider effectGraphProvider) {
        for (var entry : effectGraphProvider.getEffectGraph().getGraphElements().entrySet()) {
            sendProviderMessages(effectGraphProvider, entry.getValue());
        }
    }

    public void sendProviderMessages(GraphProvider provider, GraphElement createElement) {
        messagingService.sendMessage(new GraphNodeAddedMessage(createElement, provider.getContainingIntervalAware().getGlobalInterval()));
        messagingService.sendAsyncMessage(new GraphComponentDescriptorsAdded(createElement.getId(), createElement.getDescriptors(), provider.getContainingIntervalAware()));
    }

    public void sendKeyframeAddedMessage(GraphProvider provider) {
        TimelineInterval interval = provider.getContainingIntervalAware().getGlobalInterval();
        String containingElementId = provider.getContainingElementId();
        messagingService.sendAsyncMessage(new KeyframeSuccesfullyAddedMessage(provider.getId(), interval, containingElementId));
    }
}
