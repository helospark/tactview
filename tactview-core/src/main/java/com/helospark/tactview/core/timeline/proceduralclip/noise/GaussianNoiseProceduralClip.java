package com.helospark.tactview.core.timeline.proceduralclip.noise;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.RepeatableRandom;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.IndependentPixelOperation;

public class GaussianNoiseProceduralClip extends ProceduralVisualClip {
    private IndependentPixelOperation independentPixelOperation;
    private RepeatableRandom repeatableRandom;

    private DoubleProvider noiseChanceProvider;
    private DoubleProvider alphaMultiplierProvider;

    public GaussianNoiseProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(visualMediaMetadata, interval);
        this.independentPixelOperation = independentPixelOperation;
        repeatableRandom = new RepeatableRandom();
    }

    public GaussianNoiseProceduralClip(GaussianNoiseProceduralClip gaussianNoiseProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(gaussianNoiseProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(gaussianNoiseProceduralClip, this);
    }

    public GaussianNoiseProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2) {
        super(metadata, node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    public ClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
        BigDecimal currentSecond = relativePosition.getSeconds();

        double noiseChance = noiseChanceProvider.getValueAt(relativePosition);
        double alphaMultiplier = alphaMultiplierProvider.getValueAt(relativePosition);

        double[] gaussians = new double[result.getWidth() * result.getHeight() * 2];
        repeatableRandom.getNextGaussians(currentSecond, gaussians);
        double[] chances = new double[result.getWidth() * result.getHeight()];
        repeatableRandom.getNextDoubles(currentSecond, gaussians);

        independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
            int pixelIndex = x * result.getHeight() + y;
            if (chances[pixelIndex] <= noiseChance) {
                int color = (int) (gaussians[pixelIndex * 2 + 0] * 255);
                int alpha = (int) (gaussians[pixelIndex * 2 + 1] * alphaMultiplier * 255);
                result.setGreen(color, x, y);
                result.setRed(color, x, y);
                result.setBlue(color, x, y);
                result.setAlpha(alpha, x, y);
            }
        });
        return result;
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();
        noiseChanceProvider = new DoubleProvider(0, 1, new MultiKeyframeBasedDoubleInterpolator(0.5));
        alphaMultiplierProvider = new DoubleProvider(0, 1, new MultiKeyframeBasedDoubleInterpolator(0.05));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor noiseChangeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(noiseChanceProvider)
                .withName("Noise chance")
                .build();
        ValueProviderDescriptor alphaMultiplierDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(alphaMultiplierProvider)
                .withName("Alpha multiplier")
                .build();

        result.add(noiseChangeDescriptor);
        result.add(alphaMultiplierDescriptor);

        return result;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new GaussianNoiseProceduralClip(this, cloneRequestMetadata);
    }

}
