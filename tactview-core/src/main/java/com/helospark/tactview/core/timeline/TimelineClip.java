package com.helospark.tactview.core.timeline;

import static com.helospark.tactview.core.util.StaticObjectMapper.toValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.util.ReflectionUtil;

public abstract class TimelineClip implements EffectAware, IntervalAware, IntervalSettable {
    private Object fullClipLock = new Object();

    protected String id;
    protected TimelineInterval interval;
    protected TimelineClipType type;
    protected TimelineLength renderOffset = TimelineLength.ofZero();
    protected String creatorFactoryId;

    protected List<NonIntersectingIntervalList<StatelessEffect>> effectChannels = new ArrayList<>();

    protected List<ValueProviderDescriptor> valueDescriptors; // TODO: fill

    public TimelineClip(TimelineInterval interval, TimelineClipType type) {
        this.id = UUID.randomUUID().toString();
        this.interval = interval;
        this.type = type;
        initializeValueProvider();
    }

    public TimelineClip(TimelineClip clip, CloneRequestMetadata cloneRequestMetadata) {
        if (cloneRequestMetadata.isDeepCloneId()) {
            this.id = clip.id;
        } else {
            this.id = UUID.randomUUID().toString();
        }
        this.interval = clip.interval;
        this.type = clip.type;
        this.renderOffset = clip.renderOffset;
        this.creatorFactoryId = clip.creatorFactoryId;

        this.effectChannels = new ArrayList<NonIntersectingIntervalList<StatelessEffect>>(effectChannels.size());
        for (int i = 0; i < clip.effectChannels.size(); ++i) {
            this.effectChannels.add(cloneEffectList(clip.effectChannels.get(i), cloneRequestMetadata));
        }
    }

    public TimelineClip(JsonNode savedClip, LoadMetadata loadMetadata) {
        this.id = savedClip.get("id").asText();
        this.interval = toValue(savedClip, loadMetadata, "interval", TimelineInterval.class);
        this.type = toValue(savedClip, loadMetadata, "type", TimelineClipType.class);
        this.renderOffset = toValue(savedClip, loadMetadata, "renderOffset", TimelineLength.class);
        this.creatorFactoryId = savedClip.get("creatorFactoryId").asText();

        initializeValueProvider();
        ReflectionUtil.realoadSavedFields(savedClip.get("savedFields"), this, loadMetadata);
    }

    public Object generateSavedContent() {
        Map<String, Object> savedContent = new LinkedHashMap<>();

        savedContent.put("id", id);
        savedContent.put("interval", interval);
        savedContent.put("type", type);
        savedContent.put("renderOffset", renderOffset);
        savedContent.put("creatorFactoryId", creatorFactoryId);

        List<Object> generatedEffectChannels = new ArrayList<>();

        for (var effectChannel : effectChannels) {
            List<Object> generatedEffects = new ArrayList<>();

            for (var effect : effectChannel) {
                generatedEffects.add(effect.generateSavedContent());
            }

            generatedEffectChannels.add(generatedEffects);
        }

        savedContent.put("effectChannels", generatedEffectChannels);

        Map<String, Object> saveableFields = new LinkedHashMap<>();
        ReflectionUtil.collectSaveableFields(this, saveableFields);
        savedContent.put("savedFields", saveableFields);

        generateSavedContentInternal(savedContent);

        return savedContent;
    }

    private NonIntersectingIntervalList<StatelessEffect> cloneEffectList(NonIntersectingIntervalList<StatelessEffect> nonIntersectingIntervalList, CloneRequestMetadata cloneRequestMetadata) {
        NonIntersectingIntervalList<StatelessEffect> result = new NonIntersectingIntervalList<>();
        for (var effect : nonIntersectingIntervalList) {
            StatelessEffect effectClone = effect.cloneEffect(cloneRequestMetadata);
            effectClone.setParentIntervalAware(this);
            result.addInterval(effectClone);
        }
        return result;
    }

    @Override
    public TimelineInterval getInterval() {
        return interval;
    }

    public TimelineClipType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    protected final List<ValueProviderDescriptor> getDescriptors() {
        if (valueDescriptors == null) { // threads?
            valueDescriptors = getDescriptorsInternal();
        }
        return valueDescriptors;
    };

    protected abstract void initializeValueProvider();

    protected abstract List<ValueProviderDescriptor> getDescriptorsInternal();

    @Override
    public void setInterval(TimelineInterval newInterval) {
        this.interval = newInterval;
    }

    public Optional<StatelessEffect> getEffect(String effectId) {
        return effectChannels.stream()
                .flatMap(channel -> channel.stream())
                .filter(effect -> effect.getId().equals(effectId))
                .findFirst();
    }

    public Optional<Integer> getEffectChannelIndex(String effectId) {
        return getEffect(effectId)
                .map(a -> getEffectWithIndex(a));
    }

    public int getEffectWithIndex(StatelessEffect effect) {
        for (int i = 0; i < effectChannels.size(); ++i) {
            if (effectChannels.get(i).contains(effect)) {
                return i;
            }
        }
        return -1;
    }

    public boolean canAddEffectAt(int index, TimelineInterval newInterval) {
        return getChannelByIndex(index)
                .map(channel -> channel.canAddInterval(newInterval))
                .orElse(false);
    }

    public Optional<NonIntersectingIntervalList<StatelessEffect>> getChannelByIndex(int index) {
        if (index < 0 || index >= effectChannels.size()) {
            return Optional.empty();
        }
        return Optional.of(effectChannels.get(index));
    }

    private Optional<NonIntersectingIntervalList<StatelessEffect>> findChannelByEffect(StatelessEffect effect) {
        for (int i = 0; i < effectChannels.size(); ++i) {
            if (effectChannels.get(i).contains(effect)) {
                return Optional.of(effectChannels.get(i));
            }
        }
        return Optional.empty();
    }

    public int addEffectAtAnyChannel(StatelessEffect effect) {
        synchronized (getFullClipLock()) {
            int i = findOrCreateFirstChannelWhichEffectCanBeAdded(effect);
            return addEffectAtChannel(i, effect);
        }
    }

    public int addEffectAtChannel(int channelId, StatelessEffect effect) {
        while (effectChannels.size() <= channelId) {
            effectChannels.add(new NonIntersectingIntervalList<>());
        }
        effectChannels.get(channelId).addInterval(effect);
        effect.setParentIntervalAware(this);
        return channelId;
    }

    private int findOrCreateFirstChannelWhichEffectCanBeAdded(StatelessEffect effect) {
        for (int i = 0; i < effectChannels.size(); ++i) {
            NonIntersectingIntervalList<StatelessEffect> currentChannel = effectChannels.get(i);
            if (currentChannel.canAddInterval(effect.getInterval())) {
                return i;
            }
        }
        NonIntersectingIntervalList<StatelessEffect> newList = new NonIntersectingIntervalList<>();
        effectChannels.add(newList);
        return effectChannels.size() - 1;
    }

    public int moveEffect(StatelessEffect effect, TimelinePosition globalNewPosition) {
        synchronized (getFullClipLock()) {
            NonIntersectingIntervalList<StatelessEffect> originalEffectChannel = findChannelByEffect(effect).orElseThrow(() -> new IllegalArgumentException("Cannot find effect channel"));
            originalEffectChannel.remove(effect);

            TimelineInterval newInterval = newIntervalToBounds(effect, globalNewPosition);
            effect.setInterval(newInterval);

            int newChannelIndex = findOrCreateFirstChannelWhichEffectCanBeAdded(effect);
            effectChannels.get(newChannelIndex).addInterval(effect);

            return newChannelIndex;
        }
    }

    private TimelineInterval newIntervalToBounds(StatelessEffect effect, TimelinePosition globalNewPosition) {
        TimelinePosition localPosition = globalNewPosition.from(this.interval.getStartPosition());
        TimelineInterval newInterval = new TimelineInterval(localPosition, effect.getInterval().getLength());
        TimelineInterval localClipInterval = this.interval.butMoveStartPostionTo(TimelinePosition.ofZero());
        if (newInterval.getLength().greaterThan(interval.getLength())) {
            newInterval = newInterval.butWithEndPosition(newInterval.getStartPosition().add(interval.getLength()));
        }
        if (newInterval.getStartPosition().isLessThan(localClipInterval.getStartPosition())) {
            newInterval = newInterval.butMoveStartPostionTo(TimelinePosition.ofZero());
        }
        if (newInterval.getEndPosition().isGreaterThan(localClipInterval.getEndPosition())) {
            TimelinePosition difference = newInterval.getEndPosition().subtract(localClipInterval.getEndPosition());
            newInterval = newInterval.butMoveStartPostionTo(newInterval.getStartPosition().subtract(difference));
        }
        return newInterval;
    }

    public <T extends StatelessEffect> List<T> getEffectsAtGlobalPosition(TimelinePosition position, Class<T> type) {
        return getEffectsAt(position.from(interval.getStartPosition()), type); // Maybe not proper, see videoclip
    }

    public <T extends StatelessEffect> List<T> getEffectsAt(TimelinePosition position, Class<T> type) {
        return effectChannels.stream()
                .map(effectChannel -> effectChannel.getElementWithIntervalContainingPoint(position))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(effect -> type.isAssignableFrom(effect.getClass()))
                .map(effect -> type.cast(effect))
                .collect(Collectors.toList());
    }

    public StatelessEffect removeEffectById(String effectId) {
        synchronized (getFullClipLock()) {
            StatelessEffect effect = getEffect(effectId).orElseThrow(() -> new IllegalArgumentException("Cannot find effect"));
            NonIntersectingIntervalList<StatelessEffect> list = findChannelByEffect(effect).orElseThrow(() -> new IllegalArgumentException("Cannot find channel"));
            return list.remove(effect);
        }
    }

    public abstract boolean isResizable();

    public abstract TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata);

    protected void changeRenderStartPosition(TimelinePosition position, TimelinePosition globalTimelinePosition) {
        this.renderOffset = position.toLength();
        this.interval = this.interval.butWithStartPosition(globalTimelinePosition);

        getEffects()
                .stream()
                .forEach(a -> a.setInterval(a.getInterval().butAddOffset(position.negate())));
    }

    protected void changeRenderEndPosition(TimelinePosition localPosition) {
        this.interval = this.interval.butWithEndPosition(localPosition);
    }

    public List<TimelineClip> createCutClipParts(TimelinePosition globalTimelinePosition) {
        TimelinePosition localPosition = globalTimelinePosition.from(this.interval.getStartPosition());
        TimelineClip clipOne = this.cloneClip(CloneRequestMetadata.ofDefault());
        TimelineClip clipTwo = this.cloneClip(CloneRequestMetadata.ofDefault());

        clipOne.changeRenderEndPosition(globalTimelinePosition);
        clipTwo.changeRenderStartPosition(localPosition, globalTimelinePosition);

        handleEffect(localPosition, clipOne, false);
        handleEffect(TimelinePosition.ofZero(), clipTwo, true);

        return List.of(clipOne, clipTwo);
    }

    private void handleEffect(TimelinePosition clipRelativePosition, TimelineClip clip, boolean cutRight) {
        for (var effect : clip.getEffects()) {
            if (effect.getInterval().getStartPosition().isGreaterThan(clip.getInterval().getLength().toPosition()) ||
                    effect.getInterval().getEndPosition().isLessThan(TimelinePosition.ofZero())) {
                clip.removeEffectById(effect.getId());
            }
        }
        clip.interesectingEffects(clipRelativePosition.toInterval())
                .forEach(intersectingEffect -> {
                    if (cutRight) {
                        intersectingEffect.setInterval(intersectingEffect.getInterval().butWithStartPosition(clipRelativePosition));
                    } else {
                        intersectingEffect.setInterval(intersectingEffect.getInterval().butWithEndPosition(clipRelativePosition));
                    }
                });
    }

    public boolean resizeEffect(StatelessEffect effect, boolean left, TimelinePosition globalPosition) {
        synchronized (getFullClipLock()) {
            TimelinePosition localPositon = globalPosition.from(this.interval.getStartPosition());
            NonIntersectingIntervalList<StatelessEffect> channel = findChannelByEffect(effect).orElseThrow(() -> new IllegalArgumentException("No such channel"));

            TimelineInterval originalInterval = effect.getInterval();
            TimelineInterval newInterval = left ? originalInterval.butWithStartPosition(localPositon) : originalInterval.butWithEndPosition(localPositon);

            if (newInterval.getLength().getSeconds().compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
            if (newInterval.getStartPosition().isLessThan(0)) {
                return false;
            }
            if (newInterval.getEndPosition().isGreaterThan(interval.getEndPosition().from(interval.getStartPosition()))) {
                return false;
            }
            effect.notifyAfterResize();

            return channel.resize(effect, newInterval);
        }
    }

    protected abstract void generateSavedContentInternal(Map<String, Object> savedContent);

    protected List<String> getClipDependency(TimelinePosition position) {
        ArrayList<String> result = new ArrayList<>();

        List<String> clipsRequiredForEffect = getEffectsAtGlobalPosition(position, StatelessEffect.class)
                .stream()
                .flatMap(a -> a.getClipDependency(position).stream())
                .collect(Collectors.toList());

        result.addAll(clipsRequiredForEffect);

        return result;
    }

    protected List<String> getChannelDependency(TimelinePosition position) {
        ArrayList<String> result = new ArrayList<>();

        List<String> clipsRequiredForEffect = getEffectsAtGlobalPosition(position, StatelessEffect.class)
                .stream()
                .flatMap(a -> a.getChannelDependency(position).stream())
                .collect(Collectors.toList());

        result.addAll(clipsRequiredForEffect);

        return result;
    }

    public List<StatelessEffect> getEffects() {
        return effectChannels.stream()
                .flatMap(a -> a.stream())
                .collect(Collectors.toList());
    }

    public Stream<StatelessEffect> interesectingEffects(TimelineInterval inInterval) {
        return effectChannels.stream()
                .flatMap(a -> a.computeIntersectingIntervals(inInterval).stream());
    }

    public boolean isEnabled(TimelinePosition position) {
        return true;
    }

    public String getCreatorFactoryId() {
        return creatorFactoryId;
    }

    public void setCreatorFactoryId(String creatorFactoryId) {
        this.creatorFactoryId = creatorFactoryId;
    }

    public Object getFullClipLock() {
        return fullClipLock;
    }

    public NonIntersectingIntervalList<StatelessEffect> addEffectChannel(int newChannelIndex) {
        NonIntersectingIntervalList<StatelessEffect> newChannel = new NonIntersectingIntervalList<>();
        if (newChannelIndex >= effectChannels.size()) {
            effectChannels.add(newChannel);
        } else {
            effectChannels.add(newChannelIndex, newChannel);
        }
        return newChannel;
    }

    public List<NonIntersectingIntervalList<StatelessEffect>> getEffectChannels() {
        return effectChannels;
    }

    public TimelineLength getRenderOffset() {
        return renderOffset;
    }

}
