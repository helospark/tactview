package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Autowired;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DependentClipProvider;
import com.helospark.tactview.core.timeline.framemerge.FrameMergerWithMask;
import com.helospark.tactview.core.timeline.framemerge.FrameMergerWithMaskRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public abstract class StatelessVideoEffect extends StatelessEffect {
    private DependentClipProvider maskProvider;
    private BooleanProvider invertProvider;
    private FrameMergerWithMask frameMergerWithMask;

    public StatelessVideoEffect(TimelineInterval interval) {
        super(interval);
    }

    public StatelessVideoEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);

        ReflectionUtil.copyOrCloneFieldFromTo(effect, this, StatelessVideoEffect.class);
    }

    public StatelessVideoEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> result, SaveMetadata saveMetadata) {
        super.generateSavedContentInternal(result, saveMetadata);
    }

    public ReadOnlyClipImage createFrameExternal(StatelessEffectRequest request) {
        ReadOnlyClipImage result = createFrame(request);

        Optional<ReadOnlyClipImage> mask = Optional.ofNullable(request.getRequestedClips().get(maskProvider.getValueAt(request.getEffectPosition())));

        if (mask.isPresent()) {
            FrameMergerWithMaskRequest mergeRequest = FrameMergerWithMaskRequest.builder()
                    .withTop(result)
                    .withBottom(request.getCurrentFrame())
                    .withMask(mask.get())
                    .withInvert(invertProvider.getValueAt(request.getEffectPosition()))
                    .withScale(false)
                    .build();

            ClipImage maskedResult = frameMergerWithMask.mergeFramesWithMask(mergeRequest);

            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(result.getBuffer());
            result = maskedResult;
        }

        return result;
    }

    public abstract ReadOnlyClipImage createFrame(StatelessEffectRequest request);

    public boolean isLocal() {
        return true;
    }

    @Override
    public void initializeValueProvider() {
        super.initializeValueProvider();
        this.maskProvider = new DependentClipProvider(new StepStringInterpolator());
        this.invertProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        List<ValueProviderDescriptor> valueProviders = new ArrayList<>();

        ValueProviderDescriptor maskProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(maskProvider)
                .withName("Mask clip")
                .withGroup("Effect mask")
                .build();
        ValueProviderDescriptor invertProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(invertProvider)
                .withName("Invert")
                .withGroup("Effect mask")
                .build();

        valueProviders.add(maskProviderDescriptor);
        valueProviders.add(invertProviderDescriptor);
        valueProviders.addAll(super.getValueProviders());

        return valueProviders;
    }

    @Autowired
    public void setFrameMergerWithMask(FrameMergerWithMask frameMergerWithMask) {
        this.frameMergerWithMask = frameMergerWithMask;
    }

    @Override
    public List<String> getClipDependency(TimelinePosition position) {
        List<String> clipDependency = super.getClipDependency(position);

        String maskClipId = maskProvider.getValueAt(position);

        if (maskClipId != null) {
            clipDependency.add(maskClipId);
        }

        return clipDependency;
    }
}
