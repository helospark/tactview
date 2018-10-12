package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public abstract class TimelineClip implements IntervalAware {
    private String id;
    private TimelineInterval interval;
    private TimelineClipType type;

    protected List<NonIntersectingIntervalList<StatelessEffect>> effectChannels = new ArrayList<>();

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

    public abstract List<ValueProviderDescriptor> getDescriptors();

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

    public boolean moveEffect(StatelessEffect effect, TimelinePosition newPosition, int newEffectChannelId) {
        NonIntersectingIntervalList<StatelessEffect> originalEffectChannel = getChannelByIndex(newEffectChannelId).orElseThrow(() -> new IllegalArgumentException("Cannot find effect channel"));
        NonIntersectingIntervalList<StatelessEffect> newEffectChannel = findChannelByEffect(effect).orElseThrow(() -> new IllegalArgumentException("Cannot find effect channel"));
        originalEffectChannel.remove(effect);
        effect.setInterval(new TimelineInterval(newPosition, effect.getInterval().getWidth()));
        return newEffectChannel.addInterval(effect);
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

    public void removeEffectById(String effectId) {
        StatelessEffect effect = getEffect(effectId).orElseThrow(() -> new IllegalArgumentException("Cannot find effect"));
        NonIntersectingIntervalList<StatelessEffect> list = findChannelByEffect(effect).orElseThrow(() -> new IllegalArgumentException("Cannot find channel"));
        list.remove(effect);
    }

    public abstract boolean isResizable();
}
