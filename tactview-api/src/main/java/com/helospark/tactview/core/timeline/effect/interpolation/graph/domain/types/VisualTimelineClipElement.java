package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types;

import java.util.Map;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphAcceptType;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphConnectionDescriptor;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class VisualTimelineClipElement extends GraphElement {
    ConnectionIndex outputIndex = ConnectionIndex.random();
    VisualTimelineClip clip;

    public VisualTimelineClipElement(VisualTimelineClip clip) {
        this.clip = clip;
        this.outputs.put(outputIndex, new GraphConnectionDescriptor("Output", GraphAcceptType.IMAGE));
    }

    VisualTimelineClipElement(ConnectionIndex outputIndex, VisualTimelineClip clip) {
        this.clip = clip;
        this.outputIndex = outputIndex;
        this.outputs.put(outputIndex, new GraphConnectionDescriptor("Output", GraphAcceptType.IMAGE));
    }

    @Override
    public Map<ConnectionIndex, ReadOnlyClipImage> render(Map<ConnectionIndex, ReadOnlyClipImage> images, EffectGraphInputRequest request) {
        GetFrameRequest getFrameRequest = GetFrameRequest.builder()
                .withApplyEffects(request.applyEffects)
                .withLowResolutionPreview(request.lowResolutionPreview)
                .withExpectedWidth(request.expectedWidth)
                .withExpectedHeight(request.expectedHeight)
                .withPosition(request.position)
                .withRelativePosition(request.position)
                .withScale(request.scale)
                .withUseApproximatePosition(false)
                .build();

        ReadOnlyClipImage output = clip.getFrame(getFrameRequest);

        return Map.of(outputIndex, output);
    }

    @Override
    public String toString() {
        return "VisualTimelineClipElement [outputIndex=" + outputIndex + ", clip=" + clip + ", x=" + x + ", y=" + y + ", inputs=" + inputs + ", outputs=" + outputs + "]";
    }

    @Override
    public GraphElement deepClone() {
        VisualTimelineClipElement result = new VisualTimelineClipElement(outputIndex, (VisualTimelineClip) this.clip.cloneClip(CloneRequestMetadata.ofDefault()));
        copyCommonPropertiesTo(result);
        return result;
    }
}
