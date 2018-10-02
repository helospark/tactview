package com.helospark.tactview.core.timeline.effect;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.message.ClipDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.EffectDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class EffectParametersRepository {
    private MessagingService messagingService;

    private Map<String, KeyframeableEffect> idToEffectMap = new ConcurrentHashMap<>();

    public EffectParametersRepository(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostConstruct
    public void init() {
        messagingService.register(EffectDescriptorsAdded.class, message -> {
            addDescriptorsToRepository(message.getDescriptors());
        });
        messagingService.register(ClipDescriptorsAdded.class, message -> {
            addDescriptorsToRepository(message.getDescriptors());
        });
        messagingService.register(KeyframeAddedRequest.class, message -> {
            keyframeAdded(message);
        });
    }

    private void addDescriptorsToRepository(List<ValueProviderDescriptor> list) {
        list.stream()
                .map(a -> a.getKeyframeableEffect())
                .forEach(a -> idToEffectMap.put(a.getId(), a));
    }

    public void keyframeAdded(KeyframeAddedRequest message) {
        KeyframeableEffect valueToChange = idToEffectMap.get(message.getDescriptorId());
        if (valueToChange != null) {
            // todo: synchronize
            valueToChange.keyframeAdded(message.getGlobalTimelinePosition(), message.getValue());
        } else {
            System.out.println("We wanted to change " + message.getDescriptorId() + " but it was removed");
        }
    }

    public void removeKeyframe(KeyframeAddedRequest request) {
        KeyframeableEffect valueToChange = idToEffectMap.get(request.getDescriptorId());
        valueToChange.removeKeyframeAt(request.getGlobalTimelinePosition());
    }

}
