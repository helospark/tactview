package com.helospark.tactview.core.timeline.audioeffect;

import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;

public abstract class StatelessAudioEffect extends StatelessEffect {

    public StatelessAudioEffect(TimelineInterval interval) {
        super(interval);
    }

    public AudioFrameResult applyEffect(AudioEffectRequest request) {
        AudioFrameResult effect = applyEffectInternal(request);
        if (!effect.getLength().equals(request.getInput().getLength())) {
            throw new RuntimeException("Length mismatch");
        }
        return effect;
    }

    protected abstract AudioFrameResult applyEffectInternal(AudioEffectRequest input);

}
