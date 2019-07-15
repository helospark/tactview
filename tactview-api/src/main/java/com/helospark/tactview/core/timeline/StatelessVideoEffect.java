package com.helospark.tactview.core.timeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public abstract class StatelessVideoEffect extends StatelessEffect {

    public StatelessVideoEffect(TimelineInterval interval) {
        super(interval);
    }

    public StatelessVideoEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);
    }

    public StatelessVideoEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public abstract ReadOnlyClipImage createFrame(StatelessEffectRequest request);

    public boolean isLocal() {
        return true;
    }

}
