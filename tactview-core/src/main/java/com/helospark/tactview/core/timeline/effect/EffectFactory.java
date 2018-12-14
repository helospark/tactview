package com.helospark.tactview.core.timeline.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.api.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;

public interface EffectFactory {

    boolean doesSupport(CreateEffectRequest request);

    StatelessEffect createEffect(CreateEffectRequest timelineInterval);

    StatelessEffect restoreEffect(JsonNode node, LoadMetadata loadMetadata);

    String getEffectId();

    // seems uii, also think about localization
    String getEffectName();

    default String getId() {
        return getEffectId();
    }

}
