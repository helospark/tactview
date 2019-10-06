package com.helospark.tactview.core.timeline;

import java.util.Optional;

public interface TimelineManagerAccessorInterface {

    public Optional<TimelineClip> findClipById(String id);

    public Optional<TimelineClip> findClipForEffect(String effectId);

    public Optional<StatelessEffect> findEffectById(String effectId);

    public Optional<Integer> findChannelIndexForClipId(String clipId);

}
