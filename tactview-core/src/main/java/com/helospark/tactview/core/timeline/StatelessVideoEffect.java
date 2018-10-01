package com.helospark.tactview.core.timeline;

import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;

public abstract class StatelessVideoEffect extends StatelessEffect {

    public StatelessVideoEffect(TimelineInterval interval) {
        super(interval);
    }

    public abstract void fillFrame(ClipFrameResult result, StatelessEffectRequest request);

    public boolean isLocal() {
        return true;
    }

}
