package com.helospark.tactview.core.timeline.proceduralclip.graph;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraph;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.OutputElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.ReflectionUtil;

public class GraphProceduralClip extends ProceduralVisualClip {
    private GraphProvider graphProvider;

    public GraphProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval) {
        super(visualMediaMetadata, interval);
    }

    public GraphProceduralClip(GraphProceduralClip singleColorProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(singleColorProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(singleColorProceduralClip, this);
    }

    public GraphProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata) {
        super(metadata, node, loadMetadata);
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        EffectGraph effectGraph = graphProvider.getValueAt(relativePosition);

        EffectGraphInputRequest egr = EffectGraphInputRequest.builder()
                .withApplyEffects(true)
                .withExpectedWidth(request.getExpectedWidth())
                .withExpectedHeight(request.getExpectedHeight())
                .withInput(null)
                .withLowResolutionPreview(false)
                .withPosition(request.getGlobalPosition())
                .withRelativePosition(relativePosition)
                .withScale(request.getScale())
                .withUseApproximatePosition(false)
                .build();

        return effectGraph.evaluate(egr);

    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        EffectGraph effectGraph = new EffectGraph();
        effectGraph.getGraphElements().put(GraphIndex.random(), new OutputElement());

        graphProvider = new GraphProvider(effectGraph);
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor graphDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(graphProvider)
                .withName("Graph")
                .build();

        result.add(graphDescriptor);

        return result;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new GraphProceduralClip(this, cloneRequestMetadata);
    }

}
