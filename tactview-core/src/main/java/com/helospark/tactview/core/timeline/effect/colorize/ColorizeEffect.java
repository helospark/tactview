package com.helospark.tactview.core.timeline.effect.colorize;

import java.awt.Color;
import java.util.List;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.ThreadLocalProvider;

public class ColorizeEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private DoubleProvider hueChangeProvider;
    private DoubleProvider saturationChangeProvider;
    private DoubleProvider valueChangeProvider;

    public ColorizeEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public ColorizeEffect(ColorizeEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ThreadLocalProvider<float[]> floatArrayProvider = () -> new float[3];
        List<ThreadLocalProvider<?>> threadLocalProviders = List.of(floatArrayProvider);

        double hueChange = hueChangeProvider.getValueAt(request.getEffectPosition());
        double saturationChange = saturationChangeProvider.getValueAt(request.getEffectPosition());
        double valueChange = valueChangeProvider.getValueAt(request.getEffectPosition());

        return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), threadLocalProviders, pixelRequest -> {
            float[] floatArray = pixelRequest.getThreadLocal(floatArrayProvider);
            Color.RGBtoHSB(pixelRequest.input[0], pixelRequest.input[1], pixelRequest.input[2], floatArray);

            floatArray[0] = saturateBetweenInclusive(0.0f, 1.0f, (float) (floatArray[0] + hueChange));
            floatArray[1] = saturateBetweenInclusive(0.0f, 1.0f, (float) (floatArray[1] + saturationChange));
            floatArray[2] = saturateBetweenInclusive(0.0f, 1.0f, (float) (floatArray[2] + valueChange));

            int pixel = Color.HSBtoRGB(floatArray[0], floatArray[1], floatArray[2]);

            int newR = (pixel >> 16) & 0xFF;
            int newG = (pixel >> 8) & 0xFF;
            int newB = (pixel >> 0) & 0xFF;

            pixelRequest.output[0] = newR;
            pixelRequest.output[1] = newG;
            pixelRequest.output[2] = newB;
            pixelRequest.output[3] = pixelRequest.input[3];
        });
    }

    private float saturateBetweenInclusive(float low, float high, float value) {
        if (value < low) {
            return low;
        }
        if (value > high) {
            return high;
        }
        return value;
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        hueChangeProvider = new DoubleProvider(-1.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.0));
        saturationChangeProvider = new DoubleProvider(-1.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.0));
        valueChangeProvider = new DoubleProvider(-1.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.0));

        ValueProviderDescriptor hueDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(hueChangeProvider)
                .withName("Hue")
                .build();

        ValueProviderDescriptor saturationDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(saturationChangeProvider)
                .withName("Saturation")
                .build();

        ValueProviderDescriptor valueDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(valueChangeProvider)
                .withName("Value")
                .build();

        return List.of(hueDescriptor, saturationDescriptor, valueDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new ColorizeEffect(this);
    }

}
