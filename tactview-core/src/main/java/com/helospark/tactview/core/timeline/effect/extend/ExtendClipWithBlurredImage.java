package com.helospark.tactview.core.timeline.effect.extend;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.blur.BlurRequest;
import com.helospark.tactview.core.timeline.effect.blur.BlurService;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function.impl.StepInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class ExtendClipWithBlurredImage extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;
    private BlurService blurService;
    private ScaleService scaleService;

    private IntegerProvider kernelWidthProvider;
    private IntegerProvider kernelHeightProvider;
    private BooleanProvider uniformScaleProvider;

    public ExtendClipWithBlurredImage(TimelineInterval interval, IndependentPixelOperation independentPixelOperation, BlurService blurService, ScaleService scaleService) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
        this.blurService = blurService;
        this.scaleService = scaleService;
    }

    public ExtendClipWithBlurredImage(ExtendClipWithBlurredImage extendClipWithBlurredImage, CloneRequestMetadata cloneRequestMetadata) {
        super(extendClipWithBlurredImage, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(extendClipWithBlurredImage, this);
    }

    public ExtendClipWithBlurredImage(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2, BlurService blurService2, ScaleService scaleService2) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
        this.blurService = blurService2;
        this.scaleService = scaleService2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        double xScale = (double) request.getCanvasWidth() / currentFrame.getWidth();
        double yScale = (double) request.getCanvasHeight() / currentFrame.getHeight();

        double maxScale = Math.max(xScale, yScale);

        boolean uniformScale = uniformScaleProvider.getValueAt(request.getEffectPosition());

        if (uniformScale) {
            xScale = maxScale;
            yScale = maxScale;
        }

        int kernelWidth = ((int) (kernelWidthProvider.getValueAt(request.getEffectPosition()) * request.getScale())) * 2 + 1;
        int kernelHeight = ((int) (kernelHeightProvider.getValueAt(request.getEffectPosition()) * request.getScale())) * 2 + 1;

        ScaleRequest scaleRequest = ScaleRequest.builder()
                .withImage(currentFrame)
                .withNewWidth((int) (xScale * currentFrame.getWidth()))
                .withNewHeight((int) (yScale * currentFrame.getHeight()))
                .build();

        ClipImage scaledImage = scaleService.createScaledImage(scaleRequest);

        BlurRequest blurRequest = BlurRequest.builder()
                .withImage(scaledImage)
                .withKernelWidth(kernelWidth)
                .withKernelHeight(kernelHeight)
                .build();

        ClipImage result = blurService.createBlurredImage(blurRequest);

        int fromX = (request.getCanvasWidth() - currentFrame.getWidth()) / 2;
        int fromY = (request.getCanvasHeight() - currentFrame.getHeight()) / 2;

        independentPixelOperation.executePixelTransformation(currentFrame.getWidth(), currentFrame.getHeight(), (x, y) -> {
            int destinationX = fromX + x;
            int destinationY = fromY + y;
            if (result.inBounds(destinationX, destinationY)) {
                for (int i = 0; i < 4; ++i) {
                    int color = currentFrame.getColorComponentWithOffset(x, y, i);
                    result.setColorComponentByOffset(color, destinationX, destinationY, i);
                }
            }
        });

        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(scaledImage.getBuffer());

        return result;
    }

    @Override
    public void initializeValueProvider() {
        kernelWidthProvider = new IntegerProvider(0, 100, new MultiKeyframeBasedDoubleInterpolator(30.0));
        kernelHeightProvider = new IntegerProvider(0, 100, new MultiKeyframeBasedDoubleInterpolator(30.0));
        uniformScaleProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0, new StepInterpolator()));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor widthDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelWidthProvider)
                .withName("kernel width")
                .build();
        ValueProviderDescriptor heightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelHeightProvider)
                .withName("kernel height")
                .build();
        ValueProviderDescriptor uniformScaleDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(uniformScaleProvider)
                .withName("uniform scale")
                .build();

        return List.of(widthDescriptor, heightDescriptor, uniformScaleDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new ExtendClipWithBlurredImage(this, cloneRequestMetadata);
    }

}
