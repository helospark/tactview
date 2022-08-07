package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.EffectAware;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraph;
import com.helospark.tactview.core.util.DesSerFactory;

public class GraphProvider extends KeyframeableEffect<EffectGraph> {
    EffectGraph effectGraph;
    EffectAware containingIntervalAware;
    String containingElementId;

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

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        // TODO!
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
    public GraphProvider deepCloneInternal(CloneRequestMetadata cloneRequestMetadata) {
        return new GraphProvider(effectGraph.deepClone());
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect<EffectGraph>>> generateSerializableContent() {
        return GraphProviderFactory.class;
    }

    public void setContainingIntervalAware(EffectAware containingIntervalAware) {
        this.containingIntervalAware = containingIntervalAware;
    }

    public EffectAware getContainingIntervalAware() {
        return containingIntervalAware;
    }

    public String getContainingElementId() {
        return containingElementId;
    }

    public void setContainingElementId(String containingElementId) {
        this.containingElementId = containingElementId;
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        // TODO!
    }

    @Override
    public Class<?> getProvidedType() {
        return EffectGraph.class;
    }

}
