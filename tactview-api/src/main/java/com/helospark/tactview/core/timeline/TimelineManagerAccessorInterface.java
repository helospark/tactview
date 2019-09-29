package com.helospark.tactview.core.timeline;

import java.util.Optional;

public interface TimelineManagerAccessorInterface {

    public Optional<StatelessEffect> findEffectById(String effectId);

    public Optional<Integer> findChannelIndexForClipId(String clipId);

}
