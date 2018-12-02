package com.helospark.tactview.core.timeline;

import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;

public abstract class StatelessVideoEffect extends StatelessEffect {

    public StatelessVideoEffect(TimelineInterval interval) {
        super(interval);
    }

    public StatelessVideoEffect(StatelessVideoEffect effect) {
        super(effect);
    }

    public abstract ClipImage createFrame(StatelessEffectRequest request);

    public boolean isLocal() {
        return true;
    }

}
