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
import com.helospark.tactview.core.timeline.effect.graphing.DefaultGraphArrangementFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraph;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraphAccessorMessageSender;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.ReflectionUtil;

public class GraphProceduralClip extends ProceduralVisualClip {
    private GraphProvider graphProvider;
    private DefaultGraphArrangementFactory defaultGraphArrangementFactory;
    private EffectGraphAccessorMessageSender effectGraphAccessor;

    public GraphProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, DefaultGraphArrangementFactory defaultGraphArrangementFactory,
            EffectGraphAccessorMessageSender effectGraphAccessor) {
        super(visualMediaMetadata, interval);
        this.defaultGraphArrangementFactory = defaultGraphArrangementFactory;
        this.effectGraphAccessor = effectGraphAccessor;
    }

    public GraphProceduralClip(GraphProceduralClip proceuduralClip, CloneRequestMetadata cloneRequestMetadata, EffectGraphAccessorMessageSender effectGraphAccessor) {
        super(proceuduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(proceuduralClip, this);
    }

    public GraphProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, DefaultGraphArrangementFactory defaultGraphArrangementFactory,
            EffectGraphAccessorMessageSender effectGraphAccessor) {
        super(metadata, node, loadMetadata);
        this.defaultGraphArrangementFactory = defaultGraphArrangementFactory;
        this.effectGraphAccessor = effectGraphAccessor;
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

    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        // TODO: this should be in the initialize, but cannot set defaultGraphArrangementFactory before super call in Java :(
        // if is there to distinguish between load and new init
        if (graphProvider == null) {
            EffectGraph effectGraph = defaultGraphArrangementFactory.createEffectGraphProviderWithOutput();
            graphProvider = new GraphProvider(effectGraph);
        }
        graphProvider.setContainingIntervalAware(this);
        graphProvider.setContainingElementId(this.getId());
        effectGraphAccessor.sendProviderMessageFor(graphProvider); // TODO: this is not a pretty solution, but current arch does not provider better, must think of different way

        ValueProviderDescriptor graphDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(graphProvider)
                .withName("Graph")
                .build();

        result.add(graphDescriptor);

        return result;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new GraphProceduralClip(this, cloneRequestMetadata, effectGraphAccessor);
    }

}
