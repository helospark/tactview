package com.helospark.tactview.core.timeline.effect.sharpen;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.sharpen.implementation.OpenCVSharpenImplementation;
import com.helospark.tactview.core.timeline.effect.sharpen.implementation.OpenCVSharpenRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class SharpenEffect extends StatelessVideoEffect {
    private OpenCVSharpenImplementation openCVSharpenImplementation;

    private IntegerProvider blurRadiusProvider;
    private DoubleProvider strengthProvider;

    public SharpenEffect(TimelineInterval interval, OpenCVSharpenImplementation openCVSharpenImplementation) {
        super(interval);
        this.openCVSharpenImplementation = openCVSharpenImplementation;
    }

    public SharpenEffect(JsonNode node, LoadMetadata loadMetadata, OpenCVSharpenImplementation openCVSharpenImplementation) {
        super(node, loadMetadata);
        this.openCVSharpenImplementation = openCVSharpenImplementation;
    }

    public SharpenEffect(SharpenEffect sharpenEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(sharpenEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(sharpenEffect, this, cloneRequestMetadata);
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ClipImage result = ClipImage.sameSizeAs(request.getCurrentFrame());

        int blurRadius = blurRadiusProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext()) * 2 + 1;
        double strength = strengthProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext());

        OpenCVSharpenRequest sharpenRequest = new OpenCVSharpenRequest();
        sharpenRequest.width = request.getCurrentFrame().getWidth();
        sharpenRequest.height = request.getCurrentFrame().getHeight();
        sharpenRequest.input = request.getCurrentFrame().getBuffer();
        sharpenRequest.output = result.getBuffer();

        sharpenRequest.blurRadius = blurRadius;
        sharpenRequest.strength = strength;

        openCVSharpenImplementation.sharpen(sharpenRequest);

        return result;
    }

    @Override
    protected void initializeValueProviderInternal() {
        blurRadiusProvider = new IntegerProvider(1, 10, new MultiKeyframeBasedDoubleInterpolator(3.0));
        strengthProvider = new DoubleProvider(0.0, 2.0, new MultiKeyframeBasedDoubleInterpolator(0.5));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor blurRadiusProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(blurRadiusProvider)
                .withName("Blur radius")
                .build();

        ValueProviderDescriptor strengthProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(strengthProvider)
                .withName("strength")
                .build();

        return List.of(blurRadiusProviderDescriptor, strengthProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new SharpenEffect(this, cloneRequestMetadata);
    }

}
