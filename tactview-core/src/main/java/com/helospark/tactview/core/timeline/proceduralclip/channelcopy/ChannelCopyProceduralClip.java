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
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DependentChannelIdProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.ReflectionUtil;

public class ChannelCopyProceduralClip extends ProceduralVisualClip {
    private DependentChannelIdProvider channelIdProvider;

    public ChannelCopyProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval) {
        super(visualMediaMetadata, interval);
    }

    public ChannelCopyProceduralClip(ChannelCopyProceduralClip channelCopyProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(channelCopyProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(channelCopyProceduralClip, this);
    }

    public ChannelCopyProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata) {
        super(metadata, node, loadMetadata);
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        Optional<ReadOnlyClipImage> channelClip = channelIdProvider.getValueAt(relativePosition, request.getRequestedChannelClips());

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
    protected void initializeValueProvider() {
        super.initializeValueProvider();
        channelIdProvider = new DependentChannelIdProvider(new StepStringInterpolator());
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor channelToCopyDescriptor = ValueProviderDescriptor.builder()
                .withName("Channel to copy")
                .withKeyframeableEffect(channelIdProvider)
                .build();

        result.add(channelToCopyDescriptor);

        return result;
    }

    @Override
    public List<String> getChannelDependency(TimelinePosition position) {
        List<String> result = super.getChannelDependency(position);
        result.add(channelIdProvider.getValueAt(position));
        return result;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new ChannelCopyProceduralClip(this, cloneRequestMetadata);
    }

}
