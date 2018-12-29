package com.helospark.tactview.core.timeline.effect.desaturize;

import static com.helospark.tactview.core.timeline.effect.interpolation.ColorPickerType.CIRCLE;
import static com.helospark.tactview.core.timeline.effect.interpolation.RenderTypeHint.TYPE;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class ExclusiveDesaturizeEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private ColorProvider excludedColorProvider;
    private DoubleProvider excludedHueRangeProvider;
    private DoubleProvider falloffFactorProvider;

    public ExclusiveDesaturizeEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public ExclusiveDesaturizeEffect(ExclusiveDesaturizeEffect exclusiveDesaturizeEffect) {
        super(exclusiveDesaturizeEffect);
        ReflectionUtil.copyOrCloneFieldFromTo(exclusiveDesaturizeEffect, this);
    }

    public ExclusiveDesaturizeEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        double excludedHue = excludedColorProvider.getValueAt(request.getEffectPosition()).rgbToHsbColor().red;
        double excludedHueRange = excludedHueRangeProvider.getValueAt(request.getEffectPosition());
        double falloffFactor = falloffFactorProvider.getValueAt(request.getEffectPosition());

        return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), List.of(), pixelRequest -> {
            Color color = new Color(pixelRequest.input[0] / 255.0, pixelRequest.input[1] / 255.0, pixelRequest.input[2] / 255.0);
            Color hsbColor = color.rgbToHsbColor();
            double currentHue = hsbColor.red;

            double desaturizationFactor = 1.0 - clamp(Math.abs(currentHue - excludedHue) / excludedHueRange, 0.0, 1.0);
            desaturizationFactor = Math.pow(desaturizationFactor, falloffFactor);

            int desaturizedColor = (pixelRequest.input[0] + pixelRequest.input[1] + pixelRequest.input[2]) / 3;

            pixelRequest.output[0] = (int) (pixelRequest.input[0] * desaturizationFactor + desaturizedColor * (1.0 - desaturizationFactor));
            pixelRequest.output[1] = (int) (pixelRequest.input[1] * desaturizationFactor + desaturizedColor * (1.0 - desaturizationFactor));
            pixelRequest.output[2] = (int) (pixelRequest.input[2] * desaturizationFactor + desaturizedColor * (1.0 - desaturizationFactor));
            pixelRequest.output[3] = pixelRequest.input[3];
        });
    }

    private double clamp(double d, double min, double max) {
        if (d > max) {
            return max;
        }
        if (d < min) {
            return min;
        }
        return d;
    }

    @Override
    public void initializeValueProvider() {
        excludedColorProvider = new ColorProvider(createDoubleProvider(0.0), createDoubleProvider(1.0), createDoubleProvider(0.0));
        excludedHueRangeProvider = new DoubleProvider(0.0, 2.0, new MultiKeyframeBasedDoubleInterpolator(0.1));
        falloffFactorProvider = new DoubleProvider(0.0, 10.0, new MultiKeyframeBasedDoubleInterpolator(2.0));
    }

    private DoubleProvider createDoubleProvider(double d) {
        return new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(d));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor valueProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(excludedColorProvider)
                .withName("Excluded color")
                .withRenderHints(Collections.singletonMap(TYPE, CIRCLE))
                .build();

        ValueProviderDescriptor excludedHueRangeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(excludedHueRangeProvider)
                .withName("Excluded hue range")
                .build();

        ValueProviderDescriptor falloffFactoryDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(falloffFactorProvider)
                .withName("Falloff")
                .build();

        return List.of(valueProviderDescriptor, excludedHueRangeDescriptor, falloffFactoryDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new ExclusiveDesaturizeEffect(this);
    }

}
