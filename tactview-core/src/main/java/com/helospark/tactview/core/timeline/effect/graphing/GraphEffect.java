package com.helospark.tactview.core.timeline.effect.graphing;

import java.util.ArrayList;
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
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.InputElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.OutputElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class GraphEffect extends StatelessVideoEffect {
    private GraphProvider effectGraphProvider;

    public GraphEffect(TimelineInterval interval) {
        super(interval);
    }

    public GraphEffect(GraphEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public GraphEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        EffectGraph effectGraph = new EffectGraph();

        InputElement inputElement = new InputElement();
        OutputElement outputElement = new OutputElement();

        effectGraph.getGraphElements().put(GraphIndex.random(), inputElement);
        effectGraph.getGraphElements().put(GraphIndex.random(), outputElement);
        effectGraph.getConnections().put(inputElement.getOutputIndex(), new ArrayList<>(List.of(outputElement.getInputIndex())));
        effectGraph.autoArrangeUi();

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
