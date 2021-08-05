package com.helospark.tactview.core.timeline.effect.framehold;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

// Known limitation: cannot be used on a clip which depends on other clips
public class FrameHoldEffect extends StatelessVideoEffect {

    public FrameHoldEffect(TimelineInterval interval) {
        super(interval);
    }

    public FrameHoldEffect(FrameHoldEffect ghostingEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(ghostingEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(ghostingEffect, this, cloneRequestMetadata);
    }

    public FrameHoldEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        VisualTimelineClip clip = request.getCurrentTimelineClip();
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();

        GetFrameRequest frameRequest = GetFrameRequest.builder()
                .withApplyEffects(true)
                .withApplyEffectsLessThanEffectChannel(Optional.of(request.getEffectChannel()))
                .withExpectedWidth(currentFrame.getWidth())
                .withExpectedHeight(currentFrame.getHeight())
                .withPosition(getInterval().getStartPosition())
                .withScale(request.getScale())
                .build();

        ReadOnlyClipImage frame = clip.getFrame(frameRequest);

        return frame;
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
        return new FrameHoldEffect(this, cloneRequestMetadata);
    }

}
