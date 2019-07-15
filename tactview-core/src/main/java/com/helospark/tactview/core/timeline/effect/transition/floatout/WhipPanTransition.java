package com.helospark.tactview.core.timeline.effect.transition.floatout;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.blur.BlurRequest;
import com.helospark.tactview.core.timeline.effect.blur.BlurService;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class WhipPanTransition extends AbstractVideoTransitionEffect {
    private BlurService blurService;
    private IndependentPixelOperation independentPixelOperation;

    private IntegerProvider blurProvider;

    public WhipPanTransition(TimelineInterval interval, IndependentPixelOperation independentPixelOperation, BlurService blurService) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
        this.blurService = blurService;
    }

    public WhipPanTransition(WhipPanTransition cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public WhipPanTransition(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation, BlurService blurService) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
        this.blurService = blurService;
    }

    @Override
    protected ClipImage applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest request) {
        double progress = request.getProgress();

        ReadOnlyClipImage firstFrame = request.getFirstFrame();
        ReadOnlyClipImage secondFrame = request.getSecondFrame();

        ClipImage result = ClipImage.sameSizeAs(firstFrame);

        int distance = (int) (progress * firstFrame.getWidth());

        independentPixelOperation.executePixelTransformation(firstFrame.getWidth() - distance, firstFrame.getHeight(), (x, y) -> {
            int fromX = x + distance;
            int fromY = y;

            result.copyColorFrom(firstFrame, fromX, fromY, x, y);
        });
        independentPixelOperation.executePixelTransformation(distance, firstFrame.getHeight(), (x, y) -> {
            int toX = x + (result.getWidth() - distance);
            int toY = y;

            result.copyColorFrom(secondFrame, x, y, toX, toY);
        });

        BlurRequest blurRequest = BlurRequest.builder()
                .withImage(result)
                .withKernelWidth((int) (blurProvider.getValueAt(request.getEffectPosition()) * request.getScale()))
                .withKernelHeight(0)
                .build();

        ClipImage blurredResult = blurService.createBlurredImage(blurRequest);

        result.returnBuffer();

        return blurredResult;
    }

    @Override
    public void initializeValueProvider() {
        super.initializeValueProvider();

        blurProvider = new IntegerProvider(1, 1000, new MultiKeyframeBasedDoubleInterpolator(200.0));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        List<ValueProviderDescriptor> result = super.getValueProviders();

        ValueProviderDescriptor blurProviderDescriptor = ValueProviderDescriptor.builder()
                .withName("Blur")
                .withKeyframeableEffect(blurProvider)
                .build();

        result.add(blurProviderDescriptor);

        return result;
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new WhipPanTransition(this, cloneRequestMetadata);
    }

}
