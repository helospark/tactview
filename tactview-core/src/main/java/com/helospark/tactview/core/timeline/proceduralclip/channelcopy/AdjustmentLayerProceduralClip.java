package com.helospark.tactview.core.timeline.proceduralclip.channelcopy;

import java.util.List;
import java.util.Optional;

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
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.ReflectionUtil;

public class AdjustmentLayerProceduralClip extends ProceduralVisualClip {
    public static final String LAYER_ID = "ADJUSTMENT_LAYER_ID";

    private BooleanProvider hideBelowClipsProvider;

    public AdjustmentLayerProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval) {
        super(visualMediaMetadata, interval);
    }

    public AdjustmentLayerProceduralClip(AdjustmentLayerProceduralClip channelCopyProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(channelCopyProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(channelCopyProceduralClip, this, cloneRequestMetadata);
    }

    public AdjustmentLayerProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata) {
        super(metadata, node, loadMetadata);
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        Optional<ReadOnlyClipImage> channelClip = Optional.ofNullable(request.getRequestedChannelClips().get(LAYER_ID));

        if (channelClip.isPresent()) {
            ReadOnlyClipImage realChannelClip = channelClip.get();
            ClipImage result = ClipImage.sameSizeAs(realChannelClip);
            result.copyFrom(realChannelClip);
            return result;
        } else {
            ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
            return result;
        }

    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new AdjustmentLayerProceduralClip(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        hideBelowClipsProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor hideBelowClipsDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(hideBelowClipsProvider)
                .withName("Hide below clips")
                .build();

        result.add(hideBelowClipsDescriptor);

        return result;
    }

    public boolean shouldHideBelowClips(TimelinePosition position) {
        return hideBelowClipsProvider.getValueWithoutScriptAt(position);
    }

}
