package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public <T extends StatelessEffect> List<T> getEffectsAt(TimelinePosition position, Class<T> type) {
        return effectChannels.stream()
                .map(effectChannel -> effectChannel.getElementWithIntervalContainingPoint(position.subtract(renderOffset)))
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

    protected void changeRenderStartPosition(TimelinePosition position) {
        this.renderOffset = position.toLength();
        this.interval = this.interval.butWithStartPosition(position);
    }

    protected void changeRenderEndPosition(TimelinePosition localPosition) {
        this.interval = this.interval.butWithEndPosition(localPosition);
    }

    public List<TimelineClip> createCutClipParts(TimelinePosition localPosition) {
        TimelineClip clipOne = this.cloneClip();
        TimelineClip clipTwo = this.cloneClip();

        clipOne.changeRenderEndPosition(localPosition);
        clipTwo.changeRenderStartPosition(localPosition);
        // TODO: same for effects

        return List.of(clipOne, clipTwo);
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

        return channel.resize(effect, newInterval);
    }

    public void generateSavedContent() {

    }

}
