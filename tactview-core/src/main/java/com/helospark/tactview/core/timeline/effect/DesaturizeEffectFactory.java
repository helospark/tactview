package com.helospark.tactview.core.timeline.effect;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;

@Component
public class DesaturizeEffectFactory implements EffectFactory {

    @Override
    public boolean doesSupport(String effectId) {
        return true; // decide based on id
    }

    @Override
    public StatelessEffect createEffect(String effectId, TimelineInterval timelineInterval) {
        return new DesaturizeEffect(timelineInterval);
    }

}
