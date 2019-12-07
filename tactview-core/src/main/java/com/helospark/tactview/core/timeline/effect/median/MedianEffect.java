package com.helospark.tactview.core.timeline.effect.median;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.SimplePixelTransformerRequest;
import com.helospark.tactview.core.util.ThreadLocalProvider;

public class MedianEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private IntegerProvider kernelSizeProvider;

    public MedianEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public MedianEffect(MedianEffect medianEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(medianEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(medianEffect, this);
    }

    public MedianEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ClipImage result = calculateColorMedianValues(request);

        return result;
    }

    private ClipImage calculateColorMedianValues(StatelessEffectRequest request) {
        int size = kernelSizeProvider.getValueAt(request.getEffectPosition());

        ThreadLocalProvider<int[]> integerThreadLocal = () -> {
            return new int[(size * 2 + 1) * (size * 2 + 1)];
        };

        ReadOnlyClipImage currentFrame = request.getCurrentFrame();

        ClipImage hslImage = independentPixelOperation.createNewImageWithAppliedTransformation(currentFrame, pixelRequest -> {
            Color color = new Color(0.0, 0.0, 0.0);
            color.red = pixelRequest.input[0];
            color.green = pixelRequest.input[1];
            color.blue = pixelRequest.input[2];
            Color hsl = color.multiplyComponents(1.0 / 255).rgbToHsl().multiplyComponents(255);
            pixelRequest.output[0] = (int) hsl.red;
            pixelRequest.output[1] = (int) hsl.green;
            pixelRequest.output[2] = (int) hsl.blue;
        });

        ClipImage result = independentPixelOperation.createNewImageWithAppliedTransformation(currentFrame, List.of(integerThreadLocal), pixelRequest -> {
            int[] pixels = pixelRequest.getThreadLocal(integerThreadLocal);

            int hue = computeMedianColorComponent(size, hslImage, pixelRequest, pixels, 0);
            int saturation = computeMedianColorComponent(size, hslImage, pixelRequest, pixels, 1);
            int lightness = computeMedianColorComponent(size, hslImage, pixelRequest, pixels, 2);

            Color color = new Color(hue, saturation, lightness);

            Color rgbColor = color.multiplyComponents(1.0 / 255.0).hslToRgbColor().multiplyComponents(255.0);

            pixelRequest.output[0] = (int) rgbColor.red;
            pixelRequest.output[1] = (int) rgbColor.green;
            pixelRequest.output[2] = (int) rgbColor.blue;
            pixelRequest.output[3] = pixelRequest.input[3];
        });

        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(hslImage.getBuffer());
        return result;
    }

    private int computeMedianColorComponent(int size, ReadOnlyClipImage currentFrame, SimplePixelTransformerRequest pixelRequest, int[] pixels, int componentIndex) {
        int components = 0;
        for (int i = -size; i <= size; ++i) {
            for (int j = -size; j <= size; ++j) {
                int newX = pixelRequest.x + j;
                int newY = pixelRequest.y + i;
                if (currentFrame.inBounds(newX, newY)) {
                    pixels[components++] = currentFrame.getColorComponentWithOffset(newX, newY, componentIndex);
                }
            }
        }
        Arrays.sort(pixels, 0, components);
        int resultComponent = pixels[components / 2];
        return resultComponent;
    }

    @Override
    protected void initializeValueProviderInternal() {
        kernelSizeProvider = new IntegerProvider(1, 50, new MultiKeyframeBasedDoubleInterpolator(2.0));
        kernelSizeProvider.setScaleDependent();
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor kernelDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelSizeProvider)
                .withName("kernelSize")
                .build();
        return List.of(kernelDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new MedianEffect(this, cloneRequestMetadata);
    }

}
