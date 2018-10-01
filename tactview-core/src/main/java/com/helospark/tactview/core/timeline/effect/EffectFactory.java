package com.helospark.tactview.core.timeline.effect;

import com.helospark.tactview.core.timeline.StatelessEffect;

public interface EffectFactory {

    boolean doesSupport(CreateEffectRequest request);

    StatelessEffect createEffect(CreateEffectRequest timelineInterval);

    String getEffectId();

    // seems uii, also think about localization
    String getEffectName();
}
