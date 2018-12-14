package com.helospark.tactview.core.timeline.audioeffect;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.api.LoadMetadata;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.audioeffect.volume.VolumeAudioEffect;

public abstract class StatelessAudioEffect extends StatelessEffect {

    public StatelessAudioEffect(TimelineInterval interval) {
        super(interval);
    }

    public StatelessAudioEffect(VolumeAudioEffect volumeAudioEffect) {
        super(volumeAudioEffect);
    }

    public StatelessAudioEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
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
