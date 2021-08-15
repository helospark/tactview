package com.helospark.tactview.core.timeline.effect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.DoubleInterpolatorFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.stringinterpolator.StringInterpolatorFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.StringProvider;
import com.helospark.tactview.core.timeline.message.AbstractDescriptorsAddedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.core.timeline.message.KeyframeEnabledWasChangedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyAddedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyRemovedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyResetMessage;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class EffectParametersRepository {
    private final MessagingService messagingService;
    private final TimelineManagerAccessor timelineManagerAccessor;
    private final List<DoubleInterpolatorFactory> doubleInterpolatorFactories;
    private final List<StringInterpolatorFactory> stringInterpolatorFactories;
    @Slf4j
    private Logger logger;

    private final Map<String, EffectStore> allEffectIdToEffectMap = new ConcurrentHashMap<>();

    public EffectParametersRepository(MessagingService messagingService, List<DoubleInterpolatorFactory> interpolatorFactories, List<StringInterpolatorFactory> stringInterpolatorFactories,
            TimelineManagerAccessor timelineManagerAccessor) {
        this.messagingService = messagingService;
        this.doubleInterpolatorFactories = interpolatorFactories;
        this.stringInterpolatorFactories = stringInterpolatorFactories;
        this.timelineManagerAccessor = timelineManagerAccessor;
    }

    @PostConstruct
    public void init() {
        messagingService.register(AbstractDescriptorsAddedMessage.class, message -> {
            addDescriptorsToRepository(message.getDescriptors(), message.getIntervalAware(), message.getComponentId(), message.getParentId());
        });
        messagingService.register(KeyframeAddedRequest.class, message -> {
            keyframeAdded(message);
        });
    }

    public Optional<String> findContainingElementId(String keyframeableEffectId) {
        return Optional.ofNullable(allEffectIdToEffectMap.get(keyframeableEffectId)).map(a -> a.containingElementId);
    }

    private void addDescriptorsToRepository(List<ValueProviderDescriptor> list, EffectAware intervalAware, String containingElementId, Optional<String> parentId) {
        list.stream()
                .forEach(a -> {
                    allEffectIdToEffectMap.put(a.getKeyframeableEffect().getId(), new EffectStore(a.getKeyframeableEffect(), containingElementId, intervalAware, a, parentId));
                });

        list.stream()
                .map(a -> a.getKeyframeableEffect())
                .flatMap(a -> getAllKeyframeableEffects(a))
                .forEach(a -> {
                    allEffectIdToEffectMap.putIfAbsent(a.getId(), new EffectStore(a, containingElementId, intervalAware, parentId));
                });
    }

    public KeyframeableEffect getKeyframeableEffect(String keyframeableEffectId) {
        EffectStore effectStore = allEffectIdToEffectMap.get(keyframeableEffectId);
        return effectStore.effect;
    }

    public EffectInterpolator getKeyframeableValue(String keyframeableEffectId) {
        return allEffectIdToEffectMap.get(keyframeableEffectId).effect.getInterpolator();
    }

    private Stream<KeyframeableEffect> getAllKeyframeableEffects(KeyframeableEffect a) {
        Set<KeyframeableEffect> result = new HashSet<>();
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
            result.addAll(effects2);
            result.addAll(effects);
            effects.clear();
            effects.addAll(effects2);
            effects2.clear();
        }

        if (counterToAvoidInfiniteLoops >= 1000) {
            logger.error("Infinite loop why extracting effects, elements: {}", effects);
        }

        return result.stream();
    }

    public void keyframeAdded(KeyframeAddedRequest message) {
        EffectStore valueToChange = allEffectIdToEffectMap.get(message.getDescriptorId());
        if (valueToChange != null) {
            TimelinePosition relativePosition = positionToLocal(message.getGlobalTimelinePosition(), valueToChange);
            valueToChange.effect.keyframeAdded(relativePosition, message.getValue());
            valueToChange.effectAware.effectChanged(new EffectChangedRequest(valueToChange.effect.getId()));
            KeyframeSuccesfullyAddedMessage keyframeAddedMessage = new KeyframeSuccesfullyAddedMessage(message.getDescriptorId(), valueToChange.effectAware.getGlobalInterval(),
                    valueToChange.containingElementId);
            keyframeAddedMessage.setParentElementId(valueToChange.parentId);
            messagingService.sendAsyncMessage(keyframeAddedMessage);
        } else {
            System.out.println("We wanted to change " + message.getDescriptorId() + " but it was removed");
        }
    }

    public void resetToDefaultValue(String descriptorId) {
        EffectStore valueToChange = allEffectIdToEffectMap.get(descriptorId);
        if (valueToChange != null) {
            if (valueToChange.effect.isPrimitive()) {
                valueToChange.effect.getInterpolator().resetToDefaultValue();
                messagingService.sendAsyncMessage(new KeyframeSuccesfullyResetMessage(descriptorId, valueToChange.effectAware.getGlobalInterval(), valueToChange.containingElementId));
            } else {
                List<KeyframeableEffect<?>> children = valueToChange.effect.getChildren();
                children.stream()
                        .forEach(a -> resetToDefaultValue(a.getId()));
            }
        } else {
            System.out.println("We wanted to change " + descriptorId + " but it was removed");
        }

    }

    public void removeKeyframe(String id, TimelinePosition globalPosition) {
        EffectStore valueToChange = allEffectIdToEffectMap.get(id);
        valueToChange.effect.removeKeyframeAt(positionToLocal(globalPosition, valueToChange));
        valueToChange.effectAware.effectChanged(new EffectChangedRequest(valueToChange.effect.getId()));
        KeyframeSuccesfullyRemovedMessage keyframeRemovedMessage = new KeyframeSuccesfullyRemovedMessage(id, valueToChange.effectAware.getGlobalInterval(), valueToChange.containingElementId);
        keyframeRemovedMessage.setParentId(valueToChange.parentId);
        messagingService.sendAsyncMessage(keyframeRemovedMessage);
    }

    public Optional<Object> getKeyframeableEffectValue(String id, TimelinePosition position) {
        return Optional.ofNullable(allEffectIdToEffectMap.get(id))
                .map(a -> a.effect)
                .map(a -> a.getValueAt(position));
    }

    static class EffectStore {
        public KeyframeableEffect effect;
        public String containingElementId;
        public EffectAware effectAware;
        public Optional<ValueProviderDescriptor> descriptor;
        public Optional<String> parentId = Optional.empty();

        public EffectStore(KeyframeableEffect effect, String elementId, EffectAware intervalAware, Optional<String> parentId) {
            this.effect = effect;
            this.containingElementId = elementId;
            this.effectAware = intervalAware;
            this.descriptor = Optional.empty();
            this.parentId = parentId;
        }

        public EffectStore(KeyframeableEffect effect, String elementId, EffectAware intervalAware, ValueProviderDescriptor descriptor, Optional<String> parentId) {
            this.effect = effect;
            this.containingElementId = elementId;
            this.effectAware = intervalAware;
            this.descriptor = Optional.ofNullable(descriptor);
            this.parentId = parentId;
        }

        @Override
        public String toString() {
            return "EffectStore [effect=" + effect + ", containingElementId=" + containingElementId + ", effectAware=" + effectAware + ", descriptor=" + descriptor + ", parentId=" + parentId + "]";
        }

    }

    public Map<TimelinePosition, Object> getAllKeyframes(String id) {
        EffectStore valueToChange = allEffectIdToEffectMap.get(id);
        Stream<Map.Entry<TimelinePosition, Object>> stream = valueToChange.effect.getValues()
                .entrySet()
                .stream();
        return stream
                .collect(Collectors.toMap(a -> a.getKey().add(valueToChange.effectAware.getGlobalInterval().getStartPosition()), b -> b.getValue()));
    }

    public String getValueAt(String id, TimelinePosition position) {
        EffectStore valueToChange = allEffectIdToEffectMap.get(id);
        return String.valueOf(valueToChange.effect.getValueAt(positionToLocal(position, valueToChange)));
    }

    public Object getValueAtAsObject(String id, TimelinePosition position) {
        EffectStore valueToChange = allEffectIdToEffectMap.get(id);
        return valueToChange.effect.getValueAt(positionToLocal(position, valueToChange));
    }

    private TimelinePosition positionToLocal(TimelinePosition position, EffectStore valueToChange) {
        return position.from(valueToChange.effectAware.getGlobalInterval().getStartPosition());
    }

    public boolean isKeyframeAt(String id, TimelinePosition position) {
        EffectStore valueToChange = allEffectIdToEffectMap.get(id);
        return valueToChange != null && valueToChange.effect.isKeyframe(positionToLocal(position, valueToChange));
    }

    public Object getCurrentInterpolatorClone(String descriptorId) {
        EffectStore effectStore = allEffectIdToEffectMap.get(descriptorId);
        return effectStore.effect.getInterpolatorClone();
    }

    public Object getCurrentInterpolator(String descriptorId) {
        EffectStore effectStore = allEffectIdToEffectMap.get(descriptorId);
        return effectStore.effect.getInterpolator();
    }

    public void changeInterpolator(String descriptorId, String newInterpolatorId) {
        KeyframeableEffect previousProvider = allEffectIdToEffectMap.get(descriptorId).effect;
        if (previousProvider.getInterpolator() instanceof DoubleInterpolator) {
            DoubleInterpolator interpolator = doubleInterpolatorFactories.stream()
                    .filter(factory -> factory.doesSuppert(newInterpolatorId))
                    .findFirst()
                    .orElseThrow()
                    .createInterpolator(previousProvider, (DoubleInterpolator) previousProvider.getInterpolator());
            changeInterpolatorToInstance(descriptorId, interpolator);
        } else if (previousProvider instanceof StringProvider) {
            StringInterpolator interpolator = stringInterpolatorFactories.stream()
                    .filter(factory -> factory.doesSuppert(newInterpolatorId))
                    .findFirst()
                    .orElseThrow()
                    .createInterpolator((StringProvider) previousProvider);
            changeInterpolatorToInstance(descriptorId, interpolator);
        }
        // TODO: do the rest for other interpolators
    }

    public void changeInterpolatorToInstance(String descriptorId, Object interpolator) {
        EffectStore effectStore = allEffectIdToEffectMap.get(descriptorId);
        effectStore.effect.setInterpolator(interpolator);

        messagingService.sendAsyncMessage(new InterpolatorChangedMessage(descriptorId, effectStore.effectAware.getGlobalInterval(), effectStore.containingElementId));
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
        return findIntervalForValurProvider(id).map(a -> a.getStartPosition());
    }

    public Optional<TimelineInterval> findIntervalForValurProvider(String id) {
        EffectStore effectStore = allEffectIdToEffectMap.get(id);

        if (effectStore != null) {
            Optional<TimelineClip> optionalClip = timelineManagerAccessor.findClipById(effectStore.containingElementId);
            if (optionalClip.isPresent()) {
                TimelineClip clip = optionalClip.get();
                return Optional.of(clip.getInterval());
            }

            Optional<StatelessEffect> optionalEffect = timelineManagerAccessor.findEffectById(effectStore.containingElementId);
            if (optionalEffect.isPresent()) {
                StatelessEffect effect = optionalEffect.get();
                return Optional.of(effect.getGlobalInterval());
            }
        }

        return Optional.empty();
    }

}
