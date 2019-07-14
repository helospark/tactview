package com.helospark.tactview.core.timeline.effect.glow;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVBasedGaussianBlur;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVGaussianBlurRequest;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVRegion;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;

public class LightGlowEffect extends StatelessVideoEffect {
    private OpenCVBasedGaussianBlur blurImplementation;
    private IndependentPixelOperation independentPixelOperation;

    private IntegerProvider kernelWidthProvider;
    private IntegerProvider kernelHeightProvider;
    private IntegerProvider thresholdProvider;
    private DoubleProvider lightStrengthMultiplierProvider;
    private DoubleProvider blurMultiplierProvider;

    public LightGlowEffect(TimelineInterval interval, OpenCVBasedGaussianBlur blur, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.blurImplementation = blur;
        this.independentPixelOperation = independentPixelOperation;
    }

    public LightGlowEffect(LightGlowEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public LightGlowEffect(JsonNode node, LoadMetadata loadMetadata, OpenCVBasedGaussianBlur blurImplementation, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.blurImplementation = blurImplementation;
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        int threshold = thresholdProvider.getValueAt(request.getEffectPosition());
        double lightStrenghtMultiplier = lightStrengthMultiplierProvider.getValueAt(request.getEffectPosition());
        ClipImage lightParts = independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelReques -> {
            int grayValue = (pixelReques.input[0] + pixelReques.input[1] + pixelReques.input[2]) / 3;
            if (grayValue > threshold) {
                for (int i = 0; i < 4; ++i) {
                    pixelReques.output[i] = (int) (pixelReques.input[i] * lightStrenghtMultiplier);
                }
            } else {
                for (int i = 0; i < 4; ++i) {
                    pixelReques.output[i] = 0;
                }
            }
        });
        ReadOnlyClipImage bluredLightParts = ClipImage.sameSizeAs(lightParts);

        OpenCVGaussianBlurRequest nativeRequest = new OpenCVGaussianBlurRequest();
        nativeRequest.input = lightParts.getBuffer();
        nativeRequest.output = bluredLightParts.getBuffer();
        nativeRequest.width = bluredLightParts.getWidth();
        nativeRequest.height = bluredLightParts.getHeight();
        nativeRequest.kernelHeight = getScaleDependentOddIntegetValueMinimumOne(kernelWidthProvider, request.getScale(), request.getEffectPosition());
        nativeRequest.kernelWidth = getScaleDependentOddIntegetValueMinimumOne(kernelHeightProvider, request.getScale(), request.getEffectPosition());
        nativeRequest.blurRegion = createFullRegion(lightParts);

        blurImplementation.applyGaussianBlur(nativeRequest);

        Double blurMultiplier = blurMultiplierProvider.getValueAt(request.getEffectPosition());

        independentPixelOperation.executePixelTransformation(lightParts.getWidth(), lightParts.getHeight(), (x, y) -> {
            for (int i = 0; i < 4; ++i) {
                int value = (int) (bluredLightParts.getColorComponentWithOffset(x, y, i) * blurMultiplier + request.getCurrentFrame().getColorComponentWithOffset(x, y, i));
                lightParts.setColorComponentByOffset(value, x, y, i);
            }
        });
        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(bluredLightParts.getBuffer());

        return lightParts;
    }

    private int getScaleDependentOddIntegetValueMinimumOne(IntegerProvider provider, double scale, TimelinePosition effectPosition) {
        int result = (int) (provider.getValueAt(effectPosition) * scale) * 2 + 1;
        if (result < 1) {
            return 1;
        } else {
            return result;
        }
    }

    private OpenCVRegion createFullRegion(ReadOnlyClipImage lightParts) {
        OpenCVRegion result = new OpenCVRegion();
        result.x = 0;
        result.y = 0;
        result.width = lightParts.getWidth();
        result.height = lightParts.getHeight();
        return result;
    }

    @Override
    public void initializeValueProvider() {
        kernelWidthProvider = new IntegerProvider(1, 300, new MultiKeyframeBasedDoubleInterpolator(50.0));
        kernelHeightProvider = new IntegerProvider(1, 300, new MultiKeyframeBasedDoubleInterpolator(50.0));
        kernelHeightProvider.setScaleDependent();
        kernelHeightProvider.setScaleDependent();

        thresholdProvider = new IntegerProvider(1, 255, new MultiKeyframeBasedDoubleInterpolator(200.0));
        lightStrengthMultiplierProvider = new DoubleProvider(1.0, 20.0, new MultiKeyframeBasedDoubleInterpolator(1.3));
        blurMultiplierProvider = new DoubleProvider(1.0, 20.0, new MultiKeyframeBasedDoubleInterpolator(3.0));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor thresholdDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(thresholdProvider)
                .withName("threshold")
                .build();
        ValueProviderDescriptor lightStrengthMultiplierProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lightStrengthMultiplierProvider)
                .withName("Light strength multiplier")
                .build();
        ValueProviderDescriptor blurMultiplierProviderDesciptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(blurMultiplierProvider)
                .withName("Blur multiplier")
                .build();

        ValueProviderDescriptor widthDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelWidthProvider)
                .withName("kernelWidth")
                .build();

        ValueProviderDescriptor heightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelHeightProvider)
                .withName("kernelWidth")
                .build();

        return List.of(thresholdDescriptor, lightStrengthMultiplierProviderDescriptor, blurMultiplierProviderDesciptor, widthDescriptor, heightDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new LightGlowEffect(this, cloneRequestMetadata);
    }

}
