package com.helospark.tactview.core.timeline.effect.transition.shape;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskApplier;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskBetweenTwoImageApplyRequest;
import com.helospark.tactview.core.timeline.effect.layermask.impl.calculator.LayerMaskGrayscaleToAlpha;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.service.RadialGradientRequest;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.service.RadialGradientService;
import com.helospark.tactview.core.util.ReflectionUtil;

public class CircleTransition extends AbstractVideoTransitionEffect {
    private RadialGradientService radialGradientService;
    private LayerMaskApplier layerMaskApplier;
    private LayerMaskGrayscaleToAlpha layerMaskGrayscaleToAlpha;

    private DoubleProvider fuzzinessProvider;

    public CircleTransition(TimelineInterval interval, RadialGradientService radialGradientService, LayerMaskApplier layerMaskApplier, LayerMaskGrayscaleToAlpha layerMaskGrayscaleToAlpha) {
        super(interval);
        this.radialGradientService = radialGradientService;
        this.layerMaskApplier = layerMaskApplier;
        this.layerMaskGrayscaleToAlpha = layerMaskGrayscaleToAlpha;
    }

    public CircleTransition(CircleTransition whiteFlashTransition, CloneRequestMetadata cloneRequestMetadata) {
        super(whiteFlashTransition, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(whiteFlashTransition, this);
    }

    public CircleTransition(JsonNode node, LoadMetadata loadMetadata, RadialGradientService radialGradientService, LayerMaskApplier layerMaskApplier,
            LayerMaskGrayscaleToAlpha layerMaskGrayscaleToAlpha) {
        super(node, loadMetadata);
        this.radialGradientService = radialGradientService;
        this.layerMaskApplier = layerMaskApplier;
        this.layerMaskGrayscaleToAlpha = layerMaskGrayscaleToAlpha;
    }

    @Override
    protected ClipImage applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest transitionRequest) {
        double progress = transitionRequest.getProgress();

        int width = transitionRequest.getFirstFrame().getWidth();
        int height = transitionRequest.getFirstFrame().getHeight();
        double radius = progress * Math.max(width, height);
        Point center = new Point(width / 2, height / 2);
        Color endColor = new Color(1.0, 1.0, 1.0);
        Color startColor = new Color(0.0, 0.0, 0.0);

        RadialGradientRequest gradientRequest = RadialGradientRequest.builder()
                .withCenter(center)
                .withEndColor(endColor)
                .withHeight(height)
                .withInnerSaturation(1.0 - fuzzinessProvider.getValueAt(transitionRequest.getEffectPosition()))
                .withRadius(radius)
                .withStartColor(startColor)
                .withWidth(width)
                .build();
        ClipImage gradientImage = radialGradientService.createImageWithGradient(gradientRequest);

        LayerMaskBetweenTwoImageApplyRequest layerMaskRequest = LayerMaskBetweenTwoImageApplyRequest.builder()
                .withBottomFrame(transitionRequest.getSecondFrame())
                .withTopFrame(transitionRequest.getFirstFrame())
                .withCalculator(layerMaskGrayscaleToAlpha)
                .withMask(gradientImage)
                .build();

        ClipImage result = layerMaskApplier.mergeTwoImageWithLayerMask(layerMaskRequest);

        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(gradientImage.getBuffer());

        return result;
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new CircleTransition(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        super.initializeValueProviderInternal();

        fuzzinessProvider = new DoubleProvider(0.0, 0.5, new MultiKeyframeBasedDoubleInterpolator(0.1));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        List<ValueProviderDescriptor> result = super.getValueProvidersInternal();

        ValueProviderDescriptor fuzzyDescriptor = ValueProviderDescriptor.builder()
                .withName("Fuzzy border")
                .withKeyframeableEffect(fuzzinessProvider)
                .build();

        result.add(fuzzyDescriptor);

        return result;
    }

}
