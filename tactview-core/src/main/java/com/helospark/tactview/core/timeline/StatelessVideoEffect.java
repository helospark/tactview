package com.helospark.tactview.core.timeline;

import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;

public abstract class StatelessVideoEffect extends StatelessEffect {

    public StatelessVideoEffect(TimelineInterval interval) {
        super(interval);
    }

    public abstract ClipFrameResult createFrame(StatelessEffectRequest request);

    public boolean isLocal() {
        return true;
    }

}
