package com.helospark.tactview.core.timeline.effect.extend;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.render.FrameExtender;
import com.helospark.tactview.core.util.ReflectionUtil;

public class FrameExtendEffect extends StatelessVideoEffect {
    private FrameExtender frameExtender;

    public FrameExtendEffect(TimelineInterval interval, FrameExtender frameExtender) {
        super(interval);
        this.frameExtender = frameExtender;
    }

    public FrameExtendEffect(FrameExtendEffect frameExtendRequest, CloneRequestMetadata cloneRequestMetadata) {
        super(frameExtendRequest, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(frameExtendRequest, this, cloneRequestMetadata);
    }

    public FrameExtendEffect(JsonNode node, LoadMetadata loadMetadata, FrameExtender frameExtender) {
        super(node, loadMetadata);
        this.frameExtender = frameExtender;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();

        FrameExtender.FrameExtendRequest frameExtendRequest = FrameExtender.FrameExtendRequest.builder()
                .withClip(request.getCurrentTimelineClip())
                .withFrameResult(currentFrame)
                .withPreviewWidth(request.getCanvasWidth())
                .withPreviewHeight(request.getCanvasHeight())
                .withScale(request.getScale())
                .withTimelinePosition(request.getClipPosition())
                .withEvaluationContext(request.getEvaluationContext())
                .build();
        return frameExtender.expandFrame(frameExtendRequest);
    }

    @Override
    protected void initializeValueProviderInternal() {

    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        return List.of();
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new FrameExtendEffect(this, cloneRequestMetadata);
    }

}
