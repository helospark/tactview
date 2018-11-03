package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.blendmode.BlendMode;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function.impl.StepInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;

public abstract class VisualTimelineClip extends TimelineClip {
    protected VisualMediaMetadata mediaMetadata;

    protected PointProvider translatePointProvider;
    protected DoubleProvider globalClipAlphaProvider;
    protected BooleanProvider enabledProvider;
    private ValueListProvider<BlendModeValueListElement> blendModeProvider;

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

                frameResult = appliedEffectsResult;
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
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = new ArrayList<>();
        DoubleProvider translateXProvider = new DoubleProvider(SizeFunction.IMAGE_SIZE, new MultiKeyframeBasedDoubleInterpolator(0.0));
        DoubleProvider translateYProvider = new DoubleProvider(SizeFunction.IMAGE_SIZE, new MultiKeyframeBasedDoubleInterpolator(0.0));
        translateXProvider.setScaleDependent();
        translateYProvider.setScaleDependent();

        translatePointProvider = new PointProvider(translateXProvider, translateYProvider);
        globalClipAlphaProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
        enabledProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(TimelinePosition.ofZero(), 1.0, new StepInterpolator()));
        blendModeProvider = new ValueListProvider<>(createBlendModes(), new StringInterpolator("normal"));

        ValueProviderDescriptor translateDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(translatePointProvider)
                .withName("translate")
                .build();

        ValueProviderDescriptor globalClipAlphaDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(globalClipAlphaProvider)
                .withName("Global clip alpha")
                .build();

        ValueProviderDescriptor enabledDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(enabledProvider)
                .withName("Enabled")
                .build();

        ValueProviderDescriptor blendModeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(blendModeProvider)
                .withName("Blend mode")
                .build();

        result.add(translateDescriptor);
        result.add(globalClipAlphaDescriptor);
        result.add(enabledDescriptor);
        result.add(blendModeDescriptor);

        return result;
    }

    private List<BlendModeValueListElement> createBlendModes() {
        return Arrays.stream(BlendMode.values())
                .map(blendMode -> new BlendModeValueListElement(blendMode.getId(), blendMode.getId(), blendMode))
                .collect(Collectors.toList());
    }

    public boolean isEnabled(TimelinePosition position) {
        return enabledProvider.getValueAt(position);
    }

    public double getAlpha(TimelinePosition position) {
        return globalClipAlphaProvider.getValueAt(position);
    }

    public BlendMode getBlendModeAt(TimelinePosition position) {
        TimelinePosition relativePosition = position.from(this.interval.getStartPosition());
        relativePosition = relativePosition.add(renderOffset);
        return blendModeProvider.getValueAt(relativePosition).getBlendMode();
    }

}
