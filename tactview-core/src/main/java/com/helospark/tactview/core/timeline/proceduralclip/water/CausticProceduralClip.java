/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.helospark.tactview.core.timeline.proceduralclip.water;

import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.jhlabs.math.Noise;

// Adapted code from http://www.jhlabs.com/ip/filters/download.html CausticsFilter
public class CausticProceduralClip extends ProceduralVisualClip {
    double sin = Math.sin(0.1);
    double cos = Math.cos(0.1);

    private IndependentPixelOperation independentPixelOperation;

    private DoubleProvider scaleProvider;
    private DoubleProvider amountProvider;
    private DoubleProvider turbulenceProvider;
    private DoubleProvider dispersionProvider;
    private DoubleProvider timeProvider;

    private IntegerProvider brightnessProvider;
    private IntegerProvider samplesProvider;
    private IntegerProvider seedProvider;

    private ColorProvider backgroundColorProvider;

    public CausticProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(visualMediaMetadata, interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public CausticProceduralClip(CausticProceduralClip proceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(proceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(proceduralClip, this);
    }

    public CausticProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(metadata, node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        Random random = new Random(seedProvider.getValueAt(relativePosition));

        double scale = scaleProvider.getValueAt(relativePosition) * request.getScale();
        double amount = amountProvider.getValueAt(relativePosition);
        double turbulence = turbulenceProvider.getValueAt(relativePosition);
        double dispersion = dispersionProvider.getValueAt(relativePosition);
        double time = timeProvider.getValueAt(relativePosition);
        int brightness = brightnessProvider.getValueAt(relativePosition);
        int samples = samplesProvider.getValueAt(relativePosition);
        Color bgColor = backgroundColorProvider.getValueAt(relativePosition).multiplyComponents(255);

        int v = brightness / samples;
        if (v == 0)
            v = 1;

        double rs = 1.0f / scale;
        double d = 0.95f;

        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());

        independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
            result.setRed((int) bgColor.red, x, y);
            result.setGreen((int) bgColor.green, x, y);
            result.setBlue((int) bgColor.blue, x, y);
            result.setAlpha(255, x, y);
        });

        for (int y = 0; y < result.getHeight(); ++y) {
            for (int x = 0; x < result.getWidth(); ++x) {
                for (int s = 0; s < samples; s++) {
                    double sx = x + random.nextDouble();
                    double sy = y + random.nextDouble();
                    double nx = sx * rs;
                    double ny = sy * rs;
                    double xDisplacement, yDisplacement;
                    double focus = 0.1f + amount;
                    xDisplacement = evaluate(nx - d, ny, time, turbulence) - evaluate(nx + d, ny, time, turbulence);
                    yDisplacement = evaluate(nx, ny + d, time, turbulence) - evaluate(nx, ny - d, time, turbulence);

                    if (dispersion > 0) {
                        for (int c = 0; c < 3; c++) {
                            double ca = (1 + c * dispersion);
                            int srcX = (int) (sx + scale * focus * xDisplacement * ca);
                            int srcY = (int) (sy + scale * focus * yDisplacement * ca);

                            if (result.inBounds(srcX, srcY)) {
                                int r = result.getRed(srcX, srcY);
                                int g = result.getGreen(srcX, srcY);
                                int b = result.getBlue(srcX, srcY);
                                if (c == 2)
                                    r += v;
                                else if (c == 1)
                                    g += v;
                                else
                                    b += v;
                                result.setRed(r, srcX, srcY);
                                result.setGreen(g, srcX, srcY);
                                result.setBlue(b, srcX, srcY);
                            }
                        }
                    } else {
                        int srcX = (int) (sx + scale * focus * xDisplacement);
                        int srcY = (int) (sy + scale * focus * yDisplacement);

                        if (result.inBounds(srcX, srcY)) {
                            int r = result.getRed(srcX, srcY);
                            int g = result.getGreen(srcX, srcY);
                            int b = result.getBlue(srcX, srcY);
                            r += v;
                            g += v;
                            b += v;
                            result.setRed(r, srcX, srcY);
                            result.setGreen(g, srcX, srcY);
                            result.setBlue(b, srcX, srcY);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static double turbulence2(double x, double y, double time, double octaves) {
        double value = 0.0f;
        double remainder;
        double lacunarity = 2.0f;
        double f = 1.0f;
        int i;

        // to prevent "cascading" effects
        x += 371;
        y += 529;

        for (i = 0; i < (int) octaves; i++) {
            value += Noise.noise3(x, y, time) / f;
            x *= lacunarity;
            y *= lacunarity;
            f *= 2;
        }

        remainder = octaves - (int) octaves;
        if (remainder != 0)
            value += remainder * Noise.noise3(x, y, time) / f;

        return value;
    }

    private double evaluate(double x, double y, double time, double turbulence) {
        double xt = sin * x + cos * time;
        double tt = cos * x - cos * time;
        return turbulence <= 0.0001 ? Noise.noise3(xt, y, tt) : turbulence2(xt, y, tt, turbulence);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        scaleProvider = new DoubleProvider(50.0, 300.0, new MultiKeyframeBasedDoubleInterpolator(150.0));
        amountProvider = new DoubleProvider(0.0, 10.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
        turbulenceProvider = new DoubleProvider(0.0, 10.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
        dispersionProvider = new DoubleProvider(0.0, 10.0, new MultiKeyframeBasedDoubleInterpolator(0.0));

        timeProvider = new DoubleProvider(0.0, 100.0, new MultiKeyframeBasedDoubleInterpolator(0.0));
        timeProvider.setUseKeyframes(true);
        timeProvider.keyframeAdded(TimelinePosition.ofZero(), "0.0");
        timeProvider.keyframeAdded(TimelinePosition.ofSeconds(30), "10.0");

        brightnessProvider = new IntegerProvider(0, 30, new MultiKeyframeBasedDoubleInterpolator(10.0));
        samplesProvider = new IntegerProvider(0, 10, new MultiKeyframeBasedDoubleInterpolator(2.0));
        seedProvider = new IntegerProvider(-1000, 1000, new MultiKeyframeBasedDoubleInterpolator(0.0));

        backgroundColorProvider = ColorProvider.fromDefaultValue(0.2, 0.87, 0.87);
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor scaleProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(scaleProvider)
                .withName("Scale")
                .build();
        ValueProviderDescriptor amountProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(amountProvider)
                .withName("Amount")
                .build();
        ValueProviderDescriptor turbulenceProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(turbulenceProvider)
                .withName("Turbulence")
                .build();
        ValueProviderDescriptor dispersionProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(dispersionProvider)
                .withName("Dispersion")
                .build();
        ValueProviderDescriptor timeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(timeProvider)
                .withName("Time")
                .build();
        ValueProviderDescriptor brightnessProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(brightnessProvider)
                .withName("Brightness")
                .build();
        ValueProviderDescriptor samplesProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(samplesProvider)
                .withName("Samples")
                .build();
        ValueProviderDescriptor seedProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(seedProvider)
                .withName("Seed")
                .build();
        ValueProviderDescriptor backgroundColorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(backgroundColorProvider)
                .withName("Background color")
                .build();

        result.add(scaleProviderDescriptor);
        result.add(amountProviderDescriptor);
        result.add(turbulenceProviderDescriptor);
        result.add(dispersionProviderDescriptor);
        result.add(timeProviderDescriptor);
        result.add(brightnessProviderDescriptor);
        result.add(samplesProviderDescriptor);
        result.add(seedProviderDescriptor);
        result.add(backgroundColorProviderDescriptor);

        return result;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new CausticProceduralClip(this, cloneRequestMetadata);
    }

}
