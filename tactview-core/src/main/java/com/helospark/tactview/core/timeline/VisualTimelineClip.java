package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;

public abstract class VisualTimelineClip extends TimelineClip {
    protected VisualMediaMetadata mediaMetadata;

    protected PointProvider translatePointProvider;

    public VisualTimelineClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, TimelineClipType type) {
        super(interval, type);
        this.mediaMetadata = visualMediaMetadata;
    }

    public ClipFrameResult getFrame(GetFrameRequest request) {
        double scale = request.getScale();
        int width = (int) (mediaMetadata.getWidth() * scale);
        int height = (int) (mediaMetadata.getHeight() * scale);
        TimelinePosition relativePosition = request.calculateRelativePositionFrom(this);
        relativePosition = relativePosition.add(renderOffset);

        ByteBuffer frame = requestFrame(relativePosition, width, height);
        ClipFrameResult frameResult = new ClipFrameResult(frame, width, height);

        return applyEffects(relativePosition, frameResult, request);
    }

    protected ClipFrameResult applyEffects(TimelinePosition relativePosition, ClipFrameResult frameResult, GetFrameRequest frameRequest) {
        if (frameRequest.isApplyEffects()) {
            List<StatelessVideoEffect> actualEffects = getEffectsAt(relativePosition, StatelessVideoEffect.class);

            for (StatelessVideoEffect effect : actualEffects) {
                StatelessEffectRequest request = StatelessEffectRequest.builder()
                        .withClipPosition(relativePosition)
                        .withEffectPosition(relativePosition.from(effect.interval.getStartPosition()))
                        .withCurrentFrame(frameResult)
                        .withScale(frameRequest.getScale())
                        .build();

                ClipFrameResult appliedEffectsResult = effect.createFrame(request);

                GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frameResult.getBuffer());

                frameResult = appliedEffectsResult; // todo: free up bytebuffer
            }
        }
        return frameResult;
    }

    public abstract ByteBuffer requestFrame(TimelinePosition position, int width, int height);

    public List<NonIntersectingIntervalList<StatelessEffect>> getEffectChannels() {
        return effectChannels;
    }

    public abstract VisualMediaMetadata getMediaMetadata();

    public int getXPosition(TimelinePosition timelinePosition, double scale) {
        return (int) (translatePointProvider.getValueAt(timelinePosition).x * scale);
    }

    public int getYPosition(TimelinePosition timelinePosition, double scale) {
        return (int) (translatePointProvider.getValueAt(timelinePosition).y * scale);
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptors() {
        List<ValueProviderDescriptor> result = new ArrayList<>();
        DoubleProvider translateXProvider = new DoubleProvider(SizeFunction.IMAGE_SIZE, new DoubleInterpolator(0.0));
        DoubleProvider translateYProvider = new DoubleProvider(SizeFunction.IMAGE_SIZE, new DoubleInterpolator(0.0));
        translateXProvider.setScaleDependent();
        translateYProvider.setScaleDependent();

        translatePointProvider = new PointProvider(translateXProvider, translateYProvider);

        ValueProviderDescriptor translateDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(translatePointProvider)
                .withName("translate")
                .build();

        result.add(translateDescriptor);

        return result;
    }

}
