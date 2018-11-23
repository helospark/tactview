package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public abstract class TimelineClip implements IntervalAware, IntervalSettable {
    protected String id;
    protected TimelineInterval interval;
    protected TimelineClipType type;
    protected TimelineLength renderOffset = TimelineLength.ofZero();

    protected List<NonIntersectingIntervalList<StatelessEffect>> effectChannels = new ArrayList<>();

    protected List<ValueProviderDescriptor> valueDescriptors; // TODO: fill

    public TimelineClip(TimelineInterval interval, TimelineClipType type) {
        this.id = UUID.randomUUID().toString();
        this.interval = interval;
        this.type = type;
    }

    public TimelineClip(TimelineClip clip) {
        this.id = UUID.randomUUID().toString(); // id should not cloned
        this.interval = clip.interval;
        this.type = clip.type;
        this.renderOffset = clip.renderOffset;

        this.effectChannels = new ArrayList<NonIntersectingIntervalList<StatelessEffect>>(effectChannels.size());
        for (int i = 0; i < clip.effectChannels.size(); ++i) {
            this.effectChannels.add(cloneEffectList(clip.effectChannels.get(i)));
        }
    }

    private NonIntersectingIntervalList<StatelessEffect> cloneEffectList(NonIntersectingIntervalList<StatelessEffect> nonIntersectingIntervalList) {
        NonIntersectingIntervalList<StatelessEffect> result = new NonIntersectingIntervalList<>();
        for (var effect : nonIntersectingIntervalList) {
            StatelessEffect effectClone = effect.cloneEffect();
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

    private Optional<NonIntersectingIntervalList<StatelessEffect>> getChannelByIndex(int index) {
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
        int i = findOrCreateFirstChannelWhichEffectCanBeAdded(effect);
        effectChannels.get(i).addInterval(effect);
        effect.setParentIntervalAware(this);
        return i;
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
        NonIntersectingIntervalList<StatelessEffect> originalEffectChannel = findChannelByEffect(effect).orElseThrow(() -> new IllegalArgumentException("Cannot find effect channel"));
        originalEffectChannel.remove(effect);

        TimelineInterval newInterval = newIntervalToBounds(effect, globalNewPosition);
        effect.setInterval(newInterval);

        int newChannelIndex = findOrCreateFirstChannelWhichEffectCanBeAdded(effect);
        effectChannels.get(newChannelIndex).addInterval(effect);

        return newChannelIndex;
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
        StatelessEffect effect = getEffect(effectId).orElseThrow(() -> new IllegalArgumentException("Cannot find effect"));
        NonIntersectingIntervalList<StatelessEffect> list = findChannelByEffect(effect).orElseThrow(() -> new IllegalArgumentException("Cannot find channel"));
        return list.remove(effect);
    }

    public abstract boolean isResizable();

    protected abstract TimelineClip cloneClip();

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
        TimelineClip clipOne = this.cloneClip();
        TimelineClip clipTwo = this.cloneClip();

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
        TimelinePosition localPositon = globalPosition.from(this.interval.getStartPosition());
        NonIntersectingIntervalList<StatelessEffect> channel = findChannelByEffect(effect).orElseThrow(() -> new IllegalArgumentException("No such channel"));

        TimelineInterval originalInterval = effect.getInterval();
        TimelineInterval newInterval = left ? originalInterval.butWithStartPosition(localPositon) : originalInterval.butWithEndPosition(localPositon);

        if (newInterval.getStartPosition().isLessThan(0)) {
            return false;
        }
        if (newInterval.getEndPosition().isGreaterThan(interval.getEndPosition().from(interval.getStartPosition()))) {
            return false;
        }
        effect.notifyAfterResize();

        return channel.resize(effect, newInterval);
    }

    public void generateSavedContent() {

    }

    protected List<String> getClipDependency(TimelinePosition position) {
        ArrayList<String> result = new ArrayList<>();

        List<String> clipsRequiredForEffect = getEffectsAtGlobalPosition(position, StatelessEffect.class)
                .stream()
                .flatMap(a -> a.getClipDependency(position).stream())
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
}
