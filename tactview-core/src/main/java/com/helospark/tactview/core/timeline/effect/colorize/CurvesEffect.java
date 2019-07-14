package com.helospark.tactview.core.timeline.effect.colorize;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.CurveProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.CurveProvider.KnotAwareUnivariateFunction;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.MathUtil;

public class CurvesEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private CurveProvider curveProvider;
    private ValueListProvider<CurvesTargetValueListElement> targetProvider;

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

        KnotAwareUnivariateFunction curve = curveProvider.getValueAt(request.getEffectPosition());

        double value = 0.0;
        for (int i = 0; i < 256; ++i) {
            valueLut[i] = MathUtil.clamp((int) (curve.value(value) * 255.0), 0, 255);
            value += (1.0 / 255);
        }

        CurvesTargetFunction mappingFunction = targetProvider.getValueAt(request.getEffectPosition()).function;

        return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelRequest -> {
            mappingFunction.createResult(pixelRequest.input, pixelRequest.output, valueLut);
        });
    }

    @Override
    public void initializeValueProvider() {
        List<PointProvider> points = new ArrayList<>();
        points.add(PointProvider.of(0.0, 0.0));
        points.add(PointProvider.of(0.3, 0.2));
        points.add(PointProvider.of(0.7, 0.8));
        points.add(PointProvider.of(1.0, 1.0));

        curveProvider = new CurveProvider(0.0, 1.0, 0.0, 1.0, points);

        targetProvider = new ValueListProvider<>(createValueProviders(), new StepStringInterpolator("rgb"));
    }

    private List<CurvesTargetValueListElement> createValueProviders() {
        return List.of(
                new CurvesTargetValueListElement("rgb", (input, output, lut) -> {
                    output[0] = lut[input[0]];
                    output[1] = lut[input[1]];
                    output[2] = lut[input[2]];
                    output[3] = input[3];
                }),
                new CurvesTargetValueListElement("red", (input, output, lut) -> {
                    output[0] = lut[input[0]];
                    output[1] = input[1];
                    output[2] = input[2];
                    output[3] = input[3];
                }),
                new CurvesTargetValueListElement("green", (input, output, lut) -> {
                    output[0] = input[0];
                    output[1] = lut[input[1]];
                    output[2] = input[2];
                    output[3] = input[3];
                }),
                new CurvesTargetValueListElement("blue", (input, output, lut) -> {
                    output[0] = input[0];
                    output[1] = input[1];
                    output[2] = lut[input[2]];
                    output[3] = input[3];
                }),
                new CurvesTargetValueListElement("hue", (input, output, lut) -> {
                    Color hsl = createHslFromInput(input);
                    hsl.red = lut[(int) (hsl.red * 255.0)] / 255.0;
                    copyHslToResult(input, output, hsl);
                }),
                new CurvesTargetValueListElement("saturation", (input, output, lut) -> {
                    Color hsl = createHslFromInput(input);
                    hsl.green = lut[(int) (hsl.green * 255.0)] / 255.0;
                    copyHslToResult(input, output, hsl);
                }),
                new CurvesTargetValueListElement("lightness", (input, output, lut) -> {
                    Color hsl = createHslFromInput(input);
                    hsl.blue = lut[(int) (hsl.blue * 255.0)] / 255.0;
                    copyHslToResult(input, output, hsl);
                })

        );
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
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor targetDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(targetProvider)
                .withName("target")
                .build();
        ValueProviderDescriptor valueCurveDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(curveProvider)
                .withName("value")
                .build();

        return List.of(targetDescriptor, valueCurveDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new CurvesEffect(this, cloneRequestMetadata);
    }

    static class CurvesTargetValueListElement extends ValueListElement {
        CurvesTargetFunction function;

        public CurvesTargetValueListElement(String id, CurvesTargetFunction function) {
            super(id, id);
            this.function = function;
        }

    }

    static interface CurvesTargetFunction {
        void createResult(int[] input, int[] output, int[] lut);
    }

}
