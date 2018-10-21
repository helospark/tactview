package com.helospark.tactview.core.timeline.effect;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.IntervalAware;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.message.ClipDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.EffectDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class EffectParametersRepository {
    private MessagingService messagingService;

    private Map<String, EffectStore> idToEffectMap = new ConcurrentHashMap<>();

    public EffectParametersRepository(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostConstruct
    public void init() {
        messagingService.register(EffectDescriptorsAdded.class, message -> {
            addDescriptorsToRepository(message.getDescriptors(), message.getIntervalAware());
        });
        messagingService.register(ClipDescriptorsAdded.class, message -> {
            addDescriptorsToRepository(message.getDescriptors(), message.getClip());
        });
        messagingService.register(KeyframeAddedRequest.class, message -> {
            keyframeAdded(message);
        });
    }

    private void addDescriptorsToRepository(List<ValueProviderDescriptor> list, IntervalAware intervalAware) {
        list.stream()
                .map(a -> a.getKeyframeableEffect())
                .forEach(a -> idToEffectMap.put(a.getId(), new EffectStore(a, intervalAware)));
    }

    public void keyframeAdded(KeyframeAddedRequest message) {
        EffectStore valueToChange = idToEffectMap.get(message.getDescriptorId());
        if (valueToChange != null) {
            TimelinePosition relativePosition = message.getGlobalTimelinePosition().from(valueToChange.intervalAware.getInterval().getStartPosition());
            valueToChange.effect.keyframeAdded(relativePosition, message.getValue());
        } else {
            System.out.println("We wanted to change " + message.getDescriptorId() + " but it was removed");
        }
    }

    public void removeKeyframe(KeyframeAddedRequest request) {
        EffectStore valueToChange = idToEffectMap.get(request.getDescriptorId());
        valueToChange.effect.removeKeyframeAt(request.getGlobalTimelinePosition().from(valueToChange.intervalAware.getInterval().getStartPosition()));
    }

    static class EffectStore {
        public KeyframeableEffect effect;
        public IntervalAware intervalAware;

        public EffectStore(KeyframeableEffect effect, IntervalAware intervalAware) {
            this.effect = effect;
            this.intervalAware = intervalAware;
        }

    }
}
