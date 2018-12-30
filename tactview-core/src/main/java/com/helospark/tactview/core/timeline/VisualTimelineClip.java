package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategyAccessor;
import com.helospark.tactview.core.timeline.blendmode.BlendModeValueListElement;
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
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public abstract class VisualTimelineClip extends TimelineClip {
    protected VisualMediaMetadata mediaMetadata;

    protected PointProvider translatePointProvider;
    protected DoubleProvider globalClipAlphaProvider;
    protected BooleanProvider enabledProvider;
    protected VisualMediaSource backingSource;
    private ValueListProvider<BlendModeValueListElement> blendModeProvider;

    public VisualTimelineClip(VisualMediaMetadata mediaMetadata, TimelineInterval interval, TimelineClipType type) {
        super(interval, type);
        this.mediaMetadata = mediaMetadata;
    }

    public VisualTimelineClip(VisualTimelineClip clip) {
        super(clip);
        ReflectionUtil.copyOrCloneFieldFromTo(clip, this, VisualTimelineClip.class);
    }

    public VisualTimelineClip(VisualMediaMetadata metadata, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(savedClip, loadMetadata);
        this.mediaMetadata = metadata;
    }

    public ReadOnlyClipImage getFrame(GetFrameRequest request) {
        return getFrameInternal(request);
    }

    protected ReadOnlyClipImage getFrameInternal(GetFrameRequest request) {
        double scale = request.getScale();
        int width = (int) (mediaMetadata.getWidth() * scale);
        int height = (int) (mediaMetadata.getHeight() * scale);
        TimelinePosition relativePosition = request.calculateRelativePositionFrom(this);

        ByteBuffer frame = requestFrame(relativePosition.add(renderOffset), width, height);
        ClipImage frameResult = new ClipImage(frame, width, height);

        return applyEffects(relativePosition, frameResult, request);
    }

    protected ReadOnlyClipImage applyEffects(TimelinePosition relativePosition, ReadOnlyClipImage frameResult, GetFrameRequest frameRequest) {
        if (frameRequest.isApplyEffects()) {
            List<StatelessVideoEffect> actualEffects = getEffectsAt(relativePosition, StatelessVideoEffect.class);

            int effectChannelIndex = 0;
            for (StatelessVideoEffect effect : actualEffects) {
                if (frameRequest.getApplyEffectsLessThanEffectChannel().isPresent() && effectChannelIndex >= frameRequest.getApplyEffectsLessThanEffectChannel().get()) {
                    break;
                }

                StatelessEffectRequest request = StatelessEffectRequest.builder()
                        .withClipPosition(relativePosition)
                        .withEffectPosition(relativePosition.from(effect.interval.getStartPosition()))
                        .withCurrentFrame(frameResult)
                        .withScale(frameRequest.getScale())
                        .withRequestedClips(frameRequest.getRequestedClips())
                        .withCurrentTimelineClip(this)
                        .withEffectChannel(effectChannelIndex)
                        .build();

                ReadOnlyClipImage appliedEffectsResult = effect.createFrame(request);

                GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frameResult.getBuffer());

                frameResult = appliedEffectsResult;
                ++effectChannelIndex;
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
    protected void initializeValueProvider() {
        DoubleProvider translateXProvider = new DoubleProvider(SizeFunction.IMAGE_SIZE, new MultiKeyframeBasedDoubleInterpolator(0.0));
        DoubleProvider translateYProvider = new DoubleProvider(SizeFunction.IMAGE_SIZE, new MultiKeyframeBasedDoubleInterpolator(0.0));
        translateXProvider.setScaleDependent();
        translateYProvider.setScaleDependent();

        translatePointProvider = new PointProvider(translateXProvider, translateYProvider);
        globalClipAlphaProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
        enabledProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0, new StepInterpolator()));
        blendModeProvider = new ValueListProvider<>(createBlendModes(), new StringInterpolator("normal"));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = new ArrayList<>();

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
        return BlendModeStrategyAccessor.getStrategies()
                .stream()
                .map(blendMode -> new BlendModeValueListElement(blendMode.getId(), blendMode.getId(), blendMode))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isEnabled(TimelinePosition position) {
        return enabledProvider.getValueAt(position);
    }

    public double getAlpha(TimelinePosition position) {
        return globalClipAlphaProvider.getValueAt(position);
    }

    public BlendModeStrategy getBlendModeAt(TimelinePosition position) {
        TimelinePosition relativePosition = position.from(this.interval.getStartPosition());
        relativePosition = relativePosition.add(renderOffset);
        return blendModeProvider.getValueAt(relativePosition).getBlendMode();
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent) {
        savedContent.put("backingFile", backingSource.getBackingFile());
    }
}
