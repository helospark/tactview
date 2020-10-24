package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraph;
import com.helospark.tactview.core.util.DesSerFactory;

public class GraphProvider extends KeyframeableEffect<EffectGraph> {
    EffectGraph effectGraph;

    public GraphProvider(EffectGraph effectGraph) {
        this.effectGraph = effectGraph;
    }

    @Override
    public boolean keyframesEnabled() {
        return false;
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, EffectGraph value) {
        this.effectGraph = value;
    }

    public EffectGraph getEffectGraph() {
        return effectGraph;
    }

    @Override
    public EffectGraph getValueAt(TimelinePosition position) {
        return effectGraph;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public GraphProvider deepClone() {
        return new GraphProvider(effectGraph.deepClone());
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect<EffectGraph>>> generateSerializableContent() {
        return GraphProviderFactory.class;
    }

}
