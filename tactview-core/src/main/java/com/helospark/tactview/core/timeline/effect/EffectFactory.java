package com.helospark.tactview.core.timeline.effect;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;

public interface EffectFactory {

    boolean doesSupport(String effectId);

    StatelessEffect createEffect(String effectId, TimelineInterval timelineInterval);

}
