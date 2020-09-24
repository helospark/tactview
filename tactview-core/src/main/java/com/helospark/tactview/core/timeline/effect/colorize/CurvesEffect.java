package com.helospark.tactview.core.timeline.effect.colorize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.CurveProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.CurveProvider.KnotAwareUnivariateFunction;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.core.util.ReflectionUtil;

public class CurvesEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;
    private Map<String, CurveProviderEntry> valueProviders;

    public CurvesEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public CurvesEffect(CurvesEffect curves, CloneRequestMetadata cloneRequestMetadata) {
        super(curves, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(curves, this);
    }

    public CurvesEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        int[] valueLut = new int[256];

        ReadOnlyClipImage image = ClipImage.copyOf(request.getCurrentFrame());

        for (CurveProviderEntry curveProvider : valueProviders.values()) {
            CurveProvider provider = curveProvider.provider;

            KnotAwareUnivariateFunction curve = provider.getValueAt(request.getEffectPosition());

            double value = 0.0;
            for (int i = 0; i < 256; ++i) {
                valueLut[i] = MathUtil.clamp((int) (curve.value(value) * 255.0), 0, 255);
                value += (1.0 / 255);
            }

            CurvesTargetFunction mappingFunction = curveProvider.function;

            ReadOnlyClipImage newImage = independentPixelOperation.createNewImageWithAppliedTransformation(image, pixelRequest -> {
                mappingFunction.createResult(pixelRequest.input, pixelRequest.output, valueLut);
            });

            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(image.getBuffer());
            image = newImage;
        }
        return image;
    }

    @Override
    protected void initializeValueProviderInternal() {
        valueProviders = createValueProviders();
    }

    private Map<String, CurveProviderEntry> createValueProviders() {
        return Map.of("rgb",
                new CurveProviderEntry(
                        createCurveProvider(),
                        (input, output, lut) -> {
                            output[0] = lut[input[0]];
                            output[1] = lut[input[1]];
                            output[2] = lut[input[2]];
                            output[3] = input[3];
                        }),

                "red",
                new CurveProviderEntry(
                        createCurveProvider(),
                        (input, output, lut) -> {
                            output[0] = lut[input[0]];
                            output[1] = input[1];
                            output[2] = input[2];
                            output[3] = input[3];
                        }),

                "green",
                new CurveProviderEntry(
                        createCurveProvider(),
                        (input, output, lut) -> {
                            output[0] = input[0];
                            output[1] = lut[input[1]];
                            output[2] = input[2];
                            output[3] = input[3];
                        }),

                "blue",
                new CurveProviderEntry(
                        createCurveProvider(),
                        (input, output, lut) -> {
                            output[0] = input[0];
                            output[1] = input[1];
                            output[2] = lut[input[2]];
                            output[3] = input[3];
                        }),

                "hue",
                new CurveProviderEntry(
                        createCurveProvider(),
                        (input, output, lut) -> {
                            Color hsl = createHslFromInput(input);
                            hsl.red = lut[(int) (hsl.red * 255.0)] / 255.0;
                            copyHslToResult(input, output, hsl);
                        }),

                "saturation",
                new CurveProviderEntry(
                        createCurveProvider(),
                        (input, output, lut) -> {
                            Color hsl = createHslFromInput(input);
                            hsl.green = lut[(int) (hsl.green * 255.0)] / 255.0;
                            copyHslToResult(input, output, hsl);
                        }),

                "lightness",
                new CurveProviderEntry(
                        createCurveProvider(),
                        (input, output, lut) -> {
                            Color hsl = createHslFromInput(input);
                            hsl.blue = lut[(int) (hsl.blue * 255.0)] / 255.0;
                            copyHslToResult(input, output, hsl);
                        }));
    }

    private CurveProvider createCurveProvider() {
        List<PointProvider> points = new ArrayList<>();
        points.add(PointProvider.of(0.0, 0.0));
        points.add(PointProvider.of(0.3, 0.3));
        points.add(PointProvider.of(0.7, 0.7));
        points.add(PointProvider.of(1.0, 1.0));

        return new CurveProvider(0.0, 1.0, 0.0, 1.0, points);
    }

    private Color createHslFromInput(int[] input) {
        Color color = new Color(input[0] / 255.0, input[1] / 255.0, input[2] / 255.0);
        Color hsl = color.rgbToHsl();
        return hsl;
    }

    private void copyHslToResult(int[] input, int[] output, Color hsl) {
        Color rgbColor = hsl.hslToRgbColor().multiplyComponents(255.0);

        output[0] = (int) rgbColor.red;
        output[1] = (int) rgbColor.green;
        output[2] = (int) rgbColor.blue;
        output[3] = input[3];
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        List<ValueProviderDescriptor> result = new ArrayList<>();
        for (var entry : valueProviders.entrySet()) {

            ValueProviderDescriptor targetDescriptor = ValueProviderDescriptor.builder()
                    .withKeyframeableEffect(entry.getValue().provider)
                    .withName(entry.getKey())
                    .build();
            result.add(targetDescriptor);
        }

        return result;
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new CurvesEffect(this, cloneRequestMetadata);
    }

    static interface CurvesTargetFunction {
        void createResult(int[] input, int[] output, int[] lut);
    }

    static class CurveProviderEntry {
        CurveProvider provider;
        CurvesTargetFunction function;

        public CurveProviderEntry(CurveProvider provider, CurvesTargetFunction function) {
            this.provider = provider;
            this.function = function;
        }

    }

}
