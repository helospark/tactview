package com.helospark.tactview.core.timeline.effect.graphing;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraph;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class GraphEffect extends StatelessVideoEffect {
    private GraphProvider effectGraphProvider;
    private DefaultGraphArrangementFactory defaultGraphArrangementFactory;

    public GraphEffect(TimelineInterval interval, DefaultGraphArrangementFactory defaultGraphArrangementFactory) {
        super(interval);
        this.defaultGraphArrangementFactory = defaultGraphArrangementFactory;
    }

    public GraphEffect(GraphEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public GraphEffect(JsonNode node, LoadMetadata loadMetadata, DefaultGraphArrangementFactory defaultGraphArrangementFactory) {
        super(node, loadMetadata);
        this.defaultGraphArrangementFactory = defaultGraphArrangementFactory;
    }

    @Override
    protected void initializeValueProviderInternal() {
        EffectGraph effectGraph = defaultGraphArrangementFactory.createEffectGraphProviderWithInputAndOutput();
        effectGraphProvider = new GraphProvider(effectGraph);
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        EffectGraph effectGraph = effectGraphProvider.getEffectGraph();

        EffectGraphInputRequest egr = EffectGraphInputRequest.builder()
                .withApplyEffects(true)
                .withExpectedWidth(request.getCanvasWidth())
                .withExpectedHeight(request.getCanvasHeight())
                .withInput(request.getCurrentFrame())
                .withLowResolutionPreview(false)
                .withPosition(request.getClipPosition())
                .withRelativePosition(request.getEffectPosition())
                .withScale(request.getScale())
                .withUseApproximatePosition(false)
                .build();

        ReadOnlyClipImage result = effectGraph.evaluate(egr);

        return result;
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor graphProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(effectGraphProvider)
                .withName("Graph")
                .build();

        return List.of(graphProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GraphEffect(this, cloneRequestMetadata);
    }

}
