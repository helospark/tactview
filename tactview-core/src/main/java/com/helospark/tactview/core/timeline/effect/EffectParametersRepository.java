package com.helospark.tactview.core.timeline.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.message.InterpolatorChangedMessage;
import com.helospark.tactview.core.timeline.EffectAware;
import com.helospark.tactview.core.timeline.EffectAware.EffectChangedRequest;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.DoubleInterpolatorFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.stringinterpolator.StringInterpolatorFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.StringProvider;
import com.helospark.tactview.core.timeline.message.ClipDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.EffectDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.core.timeline.message.KeyframeEnabledWasChangedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyAddedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyRemovedMessage;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class EffectParametersRepository {
    private MessagingService messagingService;
    private TimelineManagerAccessor timelineManagerAccessor;
    private List<DoubleInterpolatorFactory> doubleInterpolatorFactories;
    private List<StringInterpolatorFactory> stringInterpolatorFactories;
    @Slf4j
    private Logger logger;

    private Map<String, EffectStore> primitiveEffectIdToEffectMap = new ConcurrentHashMap<>();
    private Map<String, EffectStore> allEffectIdToEffectMap = new ConcurrentHashMap<>();

    public EffectParametersRepository(MessagingService messagingService, List<DoubleInterpolatorFactory> interpolatorFactories, List<StringInterpolatorFactory> stringInterpolatorFactories,
            TimelineManagerAccessor timelineManagerAccessor) {
        this.messagingService = messagingService;
        this.doubleInterpolatorFactories = interpolatorFactories;
        this.stringInterpolatorFactories = stringInterpolatorFactories;
        this.timelineManagerAccessor = timelineManagerAccessor;
    }

    @PostConstruct
    public void init() {
        messagingService.register(EffectDescriptorsAdded.class, message -> {
            addDescriptorsToRepository(message.getDescriptors(), message.getEffect(), message.getEffect().getId());
        });
        messagingService.register(ClipDescriptorsAdded.class, message -> {
            addDescriptorsToRepository(message.getDescriptors(), message.getClip(), message.getClipId());
        });
        messagingService.register(KeyframeAddedRequest.class, message -> {
            keyframeAdded(message);
        });
    }

    private void addDescriptorsToRepository(List<ValueProviderDescriptor> list, EffectAware intervalAware, String containingElementId) {
        for (var element : list) {
            var a = element.getKeyframeableEffect();
            allEffectIdToEffectMap.put(a.getId(), new EffectStore(a, containingElementId, intervalAware, element));
        }

        list.stream()
                .map(a -> a.getKeyframeableEffect())
                .flatMap(a -> getPrimitiveKeyframeableEffects(a))
                .forEach(a -> {
                    primitiveEffectIdToEffectMap.put(a.getId(), new EffectStore(a, containingElementId, intervalAware));
                    //                    allEffectIdToEffectMap.put(a.getId(), new EffectStore(a, containingElementId, intervalAware));
                });
    }

    public EffectInterpolator getKeyframeableValue(String keyframeableEffectId) {
        return allEffectIdToEffectMap.get(keyframeableEffectId).effect.getInterpolator();
    }

    private Stream<KeyframeableEffect> getPrimitiveKeyframeableEffects(KeyframeableEffect a) {
        List<KeyframeableEffect> effects = new ArrayList<>();
        List<KeyframeableEffect> effects2 = new ArrayList<>();

        effects.add(a);
        boolean foundNonPrimitive = true;
        int counterToAvoidInfiniteLoops = 0;
        while (foundNonPrimitive && counterToAvoidInfiniteLoops < 1000) {
            foundNonPrimitive = false;
            for (KeyframeableEffect effect : effects) {
                if (effect.isPrimitive()) {
                    effects2.add(effect);
                } else {
                    foundNonPrimitive = true;
                    effects2.addAll(effect.getChildren());
                }
            }
            ++counterToAvoidInfiniteLoops;
            effects.clear();
            effects.addAll(effects2);
            effects2.clear();
        }

        if (counterToAvoidInfiniteLoops >= 1000) {
            logger.error("Infinite loop why extracting effects, elements: {}", effects);
        }

        return effects.stream();
    }

    public void keyframeAdded(KeyframeAddedRequest message) {
        EffectStore valueToChange = primitiveEffectIdToEffectMap.get(message.getDescriptorId());
        if (valueToChange != null) {
            TimelinePosition relativePosition = positionToLocal(message.getGlobalTimelinePosition(), valueToChange);
            valueToChange.effect.keyframeAdded(relativePosition, message.getValue());
            valueToChange.effectAware.effectChanged(new EffectChangedRequest(valueToChange.effect.getId()));
            messagingService.sendAsyncMessage(new KeyframeSuccesfullyAddedMessage(message.getDescriptorId(), valueToChange.effectAware.getGlobalInterval(), valueToChange.containingElementId));
        } else {
            System.out.println("We wanted to change " + message.getDescriptorId() + " but it was removed");
        }
    }

    public void removeKeyframe(String id, TimelinePosition globalPosition) {
        EffectStore valueToChange = primitiveEffectIdToEffectMap.get(id);
        valueToChange.effect.removeKeyframeAt(positionToLocal(globalPosition, valueToChange));
        valueToChange.effectAware.effectChanged(new EffectChangedRequest(valueToChange.effect.getId()));
        messagingService.sendAsyncMessage(new KeyframeSuccesfullyRemovedMessage(id, valueToChange.effectAware.getGlobalInterval(), valueToChange.containingElementId));
    }

    public Optional<Object> getKeyframeableEffectValue(String id, TimelinePosition position) {
        return Optional.ofNullable(primitiveEffectIdToEffectMap.get(id))
                .map(a -> a.effect)
                .map(a -> a.getValueAt(position));
    }

    static class EffectStore {
        public KeyframeableEffect effect;
        public String containingElementId;
        public EffectAware effectAware;
        public Optional<ValueProviderDescriptor> descriptor;

        public EffectStore(KeyframeableEffect effect, String elementId, EffectAware intervalAware) {
            this.effect = effect;
            this.containingElementId = elementId;
            this.effectAware = intervalAware;
            this.descriptor = Optional.empty();
        }

        public EffectStore(KeyframeableEffect effect, String elementId, EffectAware intervalAware, ValueProviderDescriptor descriptor) {
            this.effect = effect;
            this.containingElementId = elementId;
            this.effectAware = intervalAware;
            this.descriptor = Optional.ofNullable(descriptor);
        }

    }

    public Map<TimelinePosition, Object> getAllKeyframes(String id) {
        EffectStore valueToChange = primitiveEffectIdToEffectMap.get(id);
        return valueToChange.effect.getValues()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(a -> a.getKey().add(valueToChange.effectAware.getGlobalInterval().getStartPosition()), b -> b.getValue()));
    }

    public String getValueAt(String id, TimelinePosition position) {
        EffectStore valueToChange = primitiveEffectIdToEffectMap.get(id);
        return String.valueOf(valueToChange.effect.getValueAt(positionToLocal(position, valueToChange)));
    }

    public Object getValueAtAsObject(String id, TimelinePosition position) {
        EffectStore valueToChange = primitiveEffectIdToEffectMap.get(id);
        return valueToChange.effect.getValueAt(positionToLocal(position, valueToChange));
    }

    private TimelinePosition positionToLocal(TimelinePosition position, EffectStore valueToChange) {
        return position.from(valueToChange.effectAware.getGlobalInterval().getStartPosition());
    }

    public boolean isKeyframeAt(String id, TimelinePosition position) {
        EffectStore valueToChange = primitiveEffectIdToEffectMap.get(id);
        return valueToChange.effect.isKeyframe(positionToLocal(position, valueToChange));
    }

    public Object getCurrentInterpolator(String descriptorId) {
        EffectStore effectStore = primitiveEffectIdToEffectMap.get(descriptorId);
        return effectStore.effect.getInterpolatorClone();
    }

    public void changeInterpolator(String descriptorId, String newInterpolatorId) {
        KeyframeableEffect previousInterpolator = primitiveEffectIdToEffectMap.get(descriptorId).effect;
        if (previousInterpolator instanceof DoubleProvider) {
            DoubleInterpolator interpolator = doubleInterpolatorFactories.stream()
                    .filter(factory -> factory.doesSuppert(newInterpolatorId))
                    .findFirst()
                    .orElseThrow()
                    .createInterpolator((DoubleProvider) previousInterpolator);
            changeInterpolatorToInstance(descriptorId, interpolator);
        } else if (previousInterpolator instanceof StringProvider) {
            StringInterpolator interpolator = stringInterpolatorFactories.stream()
                    .filter(factory -> factory.doesSuppert(newInterpolatorId))
                    .findFirst()
                    .orElseThrow()
                    .createInterpolator((StringProvider) previousInterpolator);
            changeInterpolatorToInstance(descriptorId, interpolator);
        }
        // TODO: do the rest for other interpolators
    }

    public void changeInterpolatorToInstance(String descriptorId, Object interpolator) {
        EffectStore effectStore = primitiveEffectIdToEffectMap.get(descriptorId);
        effectStore.effect.setInterpolator(interpolator);

        messagingService.sendAsyncMessage(new InterpolatorChangedMessage(descriptorId));
    }

    public Boolean isUsingKeyframes(String keyframeableEffectId) {
        EffectStore value = allEffectIdToEffectMap.get(keyframeableEffectId);
        if (value == null) {
            return false;
        }
        return value.effect.keyframesEnabled();
    }

    public void setUsingKeyframes(String keyframeableEffectId, boolean useKeyframes) {
        EffectStore value = allEffectIdToEffectMap.get(keyframeableEffectId);
        if (value.effect.supportsKeyframes() && value.effect.keyframesEnabled() != useKeyframes) {
            value.effect.setUseKeyframes(useKeyframes);
            messagingService.sendAsyncMessage(new KeyframeEnabledWasChangedMessage(value.containingElementId, keyframeableEffectId, useKeyframes, value.effectAware.getGlobalInterval()));
        } else {
            logger.warn("Setting keyframes is called for id {}, but keyframeableInterpolator does not support it", keyframeableEffectId);
        }
    }

    public Optional<ValueProviderDescriptor> findDescriptorForLabelAndClipId(String clipId, String label) {
        return allEffectIdToEffectMap.values()
                .stream()
                .filter(a -> a.containingElementId.equals(clipId))
                .filter(a -> a.descriptor.isPresent())
                .map(a -> a.descriptor.get())
                .filter(a -> a.getName().equals(label))
                .findFirst();
    }

    public Optional<TimelinePosition> findGlobalPositionForValueProvider(String id) {
        EffectStore effectStore = allEffectIdToEffectMap.get(id);

        if (effectStore == null) {
            effectStore = primitiveEffectIdToEffectMap.get(id);
        }

        if (effectStore != null) {
            Optional<TimelineClip> optionalClip = timelineManagerAccessor.findClipById(effectStore.containingElementId);
            if (optionalClip.isPresent()) {
                TimelineClip clip = optionalClip.get();
                return Optional.of(clip.getInterval().getStartPosition());
            }

            Optional<StatelessEffect> optionalEffect = timelineManagerAccessor.findEffectById(effectStore.containingElementId);
            if (optionalEffect.isPresent()) {
                StatelessEffect effect = optionalEffect.get();
                return Optional.of(effect.getGlobalInterval().getStartPosition());
            }
        }

        return Optional.empty();
    }

}
