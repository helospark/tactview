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
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraphAccessorMessageSender;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class GraphEffect extends StatelessVideoEffect {
    private GraphProvider effectGraphProvider;
    private DefaultGraphArrangementFactory defaultGraphArrangementFactory;
    private EffectGraphAccessorMessageSender effectGraphAccessor;

    public GraphEffect(TimelineInterval interval, DefaultGraphArrangementFactory defaultGraphArrangementFactory, EffectGraphAccessorMessageSender effectGraphAccessor) {
        super(interval);
        this.defaultGraphArrangementFactory = defaultGraphArrangementFactory;
        this.effectGraphAccessor = effectGraphAccessor;
    }

    public GraphEffect(GraphEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata, EffectGraphAccessorMessageSender effectGraphAccessor) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this, cloneRequestMetadata);
    }

    public GraphEffect(JsonNode node, LoadMetadata loadMetadata, DefaultGraphArrangementFactory defaultGraphArrangementFactory, EffectGraphAccessorMessageSender effectGraphAccessor) {
        super(node, loadMetadata);
        this.defaultGraphArrangementFactory = defaultGraphArrangementFactory;
        this.effectGraphAccessor = effectGraphAccessor;
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
        effectGraphProvider.setContainingIntervalAware(this);
        effectGraphProvider.setContainingElementId(this.getId());
        effectGraphAccessor.sendProviderMessageFor(effectGraphProvider); // TODO: this is not a pretty solution, but current arch does not provider better, must think of different way

        return List.of(graphProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GraphEffect(this, cloneRequestMetadata, effectGraphAccessor);
    }

}
