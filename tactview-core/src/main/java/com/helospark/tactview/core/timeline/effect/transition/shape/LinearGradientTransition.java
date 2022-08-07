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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.layermask.LayerMaskApplier;
import com.helospark.tactview.core.timeline.effect.layermask.LayerMaskBetweenTwoImageApplyRequest;
import com.helospark.tactview.core.timeline.effect.layermask.impl.calculator.LayerMaskGrayscaleToAlpha;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.service.LinearGradientRequest;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.service.LinearGradientService;
import com.helospark.tactview.core.util.ReflectionUtil;

public class LinearGradientTransition extends AbstractVideoTransitionEffect {
    private LinearGradientService linearGradientService;
    private LayerMaskApplier layerMaskApplier;
    private LayerMaskGrayscaleToAlpha layerMaskGrayscaleToAlpha;

    private DoubleProvider fuzzinessProvider;
    private LineProvider directionProvider;

    public LinearGradientTransition(TimelineInterval interval, LinearGradientService linearGradientService, LayerMaskApplier layerMaskApplier, LayerMaskGrayscaleToAlpha layerMaskGrayscaleToAlpha) {
        super(interval);
        this.linearGradientService = linearGradientService;
        this.layerMaskApplier = layerMaskApplier;
        this.layerMaskGrayscaleToAlpha = layerMaskGrayscaleToAlpha;
    }

    public LinearGradientTransition(LinearGradientTransition whiteFlashTransition, CloneRequestMetadata cloneRequestMetadata) {
        super(whiteFlashTransition, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(whiteFlashTransition, this, cloneRequestMetadata);
    }

    public LinearGradientTransition(JsonNode node, LoadMetadata loadMetadata, LinearGradientService linearGradientService, LayerMaskApplier layerMaskApplier,
            LayerMaskGrayscaleToAlpha layerMaskGrayscaleToAlpha) {
        super(node, loadMetadata);
        this.linearGradientService = linearGradientService;
        this.layerMaskApplier = layerMaskApplier;
        this.layerMaskGrayscaleToAlpha = layerMaskGrayscaleToAlpha;
    }

    @Override
    protected ClipImage applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest transitionRequest) {
        double progress = transitionRequest.getProgress();

        int w = transitionRequest.getFirstFrame().getWidth();
        int h = transitionRequest.getFirstFrame().getHeight();

        double fuzziness = fuzzinessProvider.getValueAt(transitionRequest.getEffectPosition(), transitionRequest.getEvaluationContext());

        InterpolationLine line = directionProvider.getValueAt(transitionRequest.getEffectPosition(), transitionRequest.getEvaluationContext());
        if (line.length() <= 0.00001) {
            return ClipImage.fromSize(w, h);
        }

        Point endPosition = line.end.subtract(line.start);
        Point normalizedDirection = endPosition.scalarDivide(line.end.distanceFrom(line.start));

        Point startPosition = new Point(0.0, 0.0);

        if (normalizedDirection.x > 0.0) {
            startPosition.x = 0.0;
        } else {
            startPosition.x = 1.0;
        }
        if (normalizedDirection.y > 0.0) {
            startPosition.y = 0.0;
        } else {
            startPosition.y = 1.0;
        }

        double t = progress * 1.5;

        Point offsetStart = startPosition.add(normalizedDirection.scalarMultiply(t));
        Point offsetEnd = offsetStart.add(normalizedDirection.scalarMultiply(fuzziness + 0.001));

        LinearGradientRequest linearGradientRequest = LinearGradientRequest.builder()
                .withStartColor(new Color(0.0, 0.0, 0.0))
                .withEndColor(new Color(1.0, 1.0, 1.0))
                .withWidth(w)
                .withHeight(h)
                .withNormalizedLine(new InterpolationLine(offsetStart, offsetEnd))
                .withSaturateOnEndSide(true)
                .build();

        ClipImage gradientImageMask = linearGradientService.render(linearGradientRequest);

        LayerMaskBetweenTwoImageApplyRequest layerMaskRequest = LayerMaskBetweenTwoImageApplyRequest.builder()
                .withBottomFrame(transitionRequest.getFirstFrame())
                .withTopFrame(transitionRequest.getSecondFrame())
                .withCalculator(layerMaskGrayscaleToAlpha)
                .withMask(gradientImageMask)
                .build();

        ClipImage result = layerMaskApplier.mergeTwoImageWithLayerMask(layerMaskRequest);

        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(gradientImageMask.getBuffer());

        return result;
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new LinearGradientTransition(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        super.initializeValueProviderInternal();

        fuzzinessProvider = new DoubleProvider(0.0, 0.2, new MultiKeyframeBasedDoubleInterpolator(0.05));
        directionProvider = LineProvider.ofNormalizedScreenCoordinates(0.3, 0.3, 0.8, 0.8);
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        List<ValueProviderDescriptor> result = super.getValueProvidersInternal();

        ValueProviderDescriptor fuzzyDescriptor = ValueProviderDescriptor.builder()
                .withName("Fuzzy border")
                .withKeyframeableEffect(fuzzinessProvider)
                .build();
        ValueProviderDescriptor directionDescriptor = ValueProviderDescriptor.builder()
                .withName("Direction")
                .withKeyframeableEffect(directionProvider)
                .build();

        result.add(fuzzyDescriptor);
        result.add(directionDescriptor);

        return result;
    }

}
