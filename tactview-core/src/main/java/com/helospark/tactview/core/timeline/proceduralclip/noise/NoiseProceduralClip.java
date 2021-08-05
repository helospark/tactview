package com.helospark.tactview.core.timeline.proceduralclip.noise;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.RenderTypeHint;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.SliderValueType;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

import me.jordanpeck.FastNoise;
import me.jordanpeck.FastNoise.CellularDistanceFunction;
import me.jordanpeck.FastNoise.CellularReturnType;
import me.jordanpeck.FastNoise.FractalType;

public class NoiseProceduralClip extends ProceduralVisualClip {
    private IndependentPixelOperation independentPixelOperation;

    private ColorProvider colorProvider;

    private ValueListProvider<FractalTypeValueListElement> fractalKindProvider;
    private ValueListProvider<FractalOctaveCombinerTypeValueListElement> fractalOctaveCombinderProvider;
    private ValueListProvider<CellularReturnTypeValueListElement> cellularReturnTypeProvider;
    private ValueListProvider<CellularDistanceFunctionValueListElement> cellularDistanceFunctionProvider;

    private IntegerProvider seedProvider;

    private IntegerProvider octaveProvider;
    private DoubleProvider frequencyProvider;
    private DoubleProvider lacunarityProvider;
    private DoubleProvider gainProvider;

    private DoubleProvider xOffsetProvider;
    private DoubleProvider yOffsetProvider;

    public NoiseProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(visualMediaMetadata, interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public NoiseProceduralClip(NoiseProceduralClip noiseProceduralEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(noiseProceduralEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(noiseProceduralEffect, this, cloneRequestMetadata);
    }

    public NoiseProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(metadata, node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        int seed = seedProvider.getValueAt(relativePosition);

        FastNoise fastNoise = new FastNoise(seed);

        double frequency = frequencyProvider.getValueAt(relativePosition);
        fastNoise.SetFrequency((float) frequency);

        CellularReturnType cellularReturnType = cellularReturnTypeProvider.getValueAt(relativePosition).getCellularReturnType();
        fastNoise.SetCellularReturnType(cellularReturnType);

        FractalType octaveCombinerType = fractalOctaveCombinderProvider.getValueAt(relativePosition).getFractalType();
        fastNoise.SetFractalType(octaveCombinerType);

        int octaves = octaveProvider.getValueAt(relativePosition);
        fastNoise.SetFractalOctaves(octaves);

        double lacunarity = lacunarityProvider.getValueAt(relativePosition);
        fastNoise.SetFractalLacunarity((float) lacunarity);

        double gain = gainProvider.getValueAt(relativePosition);
        fastNoise.SetFractalGain((float) gain);

        CellularDistanceFunction cellularDistance = cellularDistanceFunctionProvider.getValueAt(relativePosition).getCellularDistanceFunction();
        fastNoise.SetCellularDistanceFunction(cellularDistance);

        int width = request.getExpectedWidth();
        int height = request.getExpectedHeight();
        ClipImage result = ClipImage.fromSize(width, height);

        float xOffset = (float) (double) xOffsetProvider.getValueAt(relativePosition);
        float yOffset = (float) (double) yOffsetProvider.getValueAt(relativePosition);

        Color color = colorProvider.getValueAt(relativePosition);

        FractalFunction fractalFunction = fractalKindProvider.getValueAt(relativePosition).getFractalFunction();

        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            float normalizedX = (float) x / width * 1000f + xOffset;
            float normalizedY = (float) y / width * 1000f + yOffset;

            float value = fractalFunction.calculateFractalValue(fastNoise, normalizedX, normalizedY);
            double noiseLevel = (((value + 1.0) / 2.0) * 255.0);

            result.setColorComponentByOffset((int) (noiseLevel * color.red), x, y, 0);
            result.setColorComponentByOffset((int) (noiseLevel * color.green), x, y, 1);
            result.setColorComponentByOffset((int) (noiseLevel * color.blue), x, y, 2);
            result.setColorComponentByOffset(255, x, y, 3);
        });

        return result;
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        fractalKindProvider = new ValueListProvider<>(createFractalTypes(), new StepStringInterpolator("perlin"));
        seedProvider = new IntegerProvider(0, 1000000, new MultiKeyframeBasedDoubleInterpolator((double) new Random().nextInt(1000000)));
        frequencyProvider = new DoubleProvider(0.00001, 1, new MultiKeyframeBasedDoubleInterpolator(0.01));
        cellularReturnTypeProvider = new ValueListProvider<>(createCellularReturnTypeElements(), new StepStringInterpolator(CellularReturnType.CellValue.name()));
        fractalOctaveCombinderProvider = new ValueListProvider<>(createFractalOctaveCombinerElements(), new StepStringInterpolator(FractalType.FBM.name()));
        octaveProvider = new IntegerProvider(1, 20, new MultiKeyframeBasedDoubleInterpolator(1.0));
        lacunarityProvider = new DoubleProvider(0.1, 10, new MultiKeyframeBasedDoubleInterpolator(1.0)); // TODO: is this doing anything?
        gainProvider = new DoubleProvider(0.1, 10, new MultiKeyframeBasedDoubleInterpolator(1.0)); // TODO: is this doing anything?
        xOffsetProvider = new DoubleProvider(-10000, 10000, new MultiKeyframeBasedDoubleInterpolator(0.0));
        yOffsetProvider = new DoubleProvider(-10000, 10000, new MultiKeyframeBasedDoubleInterpolator(0.0));
        colorProvider = new ColorProvider(createColorProviderComponent(1.0), createColorProviderComponent(1.0), createColorProviderComponent(1.0));
        cellularDistanceFunctionProvider = new ValueListProvider<>(createCellularDistanceFunctionElements(), new StepStringInterpolator(CellularDistanceFunction.Euclidean.name()));
    }

    private List<CellularDistanceFunctionValueListElement> createCellularDistanceFunctionElements() {
        return Arrays.stream(CellularDistanceFunction.values())
                .map(type -> new CellularDistanceFunctionValueListElement(type))
                .collect(Collectors.toList());
    }

    private DoubleProvider createColorProviderComponent(double d) {
        return new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(d));
    }

    private List<FractalOctaveCombinerTypeValueListElement> createFractalOctaveCombinerElements() {
        return Arrays.stream(FractalType.values())
                .map(type -> new FractalOctaveCombinerTypeValueListElement(type))
                .collect(Collectors.toList());
    }

    private List<CellularReturnTypeValueListElement> createCellularReturnTypeElements() {
        return Arrays.stream(CellularReturnType.values())
                .map(type -> new CellularReturnTypeValueListElement(type))
                .collect(Collectors.toList());
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> descriptors = super.getDescriptorsInternal();

        ValueProviderDescriptor fractalTypeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fractalKindProvider)
                .withName("type")
                .build();

        ValueProviderDescriptor seedDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(seedProvider)
                .withName("seed")
                .withRenderHints(Map.of(RenderTypeHint.TYPE, SliderValueType.INPUT_FIELD))
                .build();

        ValueProviderDescriptor xOffsetDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(xOffsetProvider)
                .withName("X offset")
                .build();
        ValueProviderDescriptor yOffsetDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(yOffsetProvider)
                .withName("Y offset")
                .build();

        ValueProviderDescriptor frequencyDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(frequencyProvider)
                .withName("frequency")
                .build();

        ValueProviderDescriptor colorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorProvider)
                .withName("color")
                .build();

        Function<TimelinePosition, Boolean> enabledIfCellular = globalPosition -> fractalKindProvider.getValueAt(globalPosition).getId().equals("cellular");
        ValueProviderDescriptor cellularReturnTypeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(cellularReturnTypeProvider)
                .withName("cell ReturnType")
                .withEnabledIf(enabledIfCellular)
                .build();
        ValueProviderDescriptor cellularDistanceDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(cellularDistanceFunctionProvider)
                .withName("cell distance")
                .withEnabledIf(enabledIfCellular)
                .build();

        Function<TimelinePosition, Boolean> enabledIfFractal = globalPosition -> fractalKindProvider.getValueAt(globalPosition).getId().contains("Fractal");
        ValueProviderDescriptor octaveDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(octaveProvider)
                .withName("fractal octave")
                .withEnabledIf(enabledIfFractal)
                .build();
        ValueProviderDescriptor octaveCombinerDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fractalOctaveCombinderProvider)
                .withName("fractal octaveCombiner")
                .withEnabledIf(enabledIfFractal)
                .build();
        ValueProviderDescriptor lacunarityDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lacunarityProvider)
                .withName("fractal lacunarity")
                .withEnabledIf(enabledIfFractal)
                .build();
        ValueProviderDescriptor gainDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(gainProvider)
                .withName("fractal gain")
                .withEnabledIf(enabledIfFractal)
                .build();

        descriptors.add(fractalTypeDescriptor);
        descriptors.add(seedDescriptor);
        descriptors.add(xOffsetDescriptor);
        descriptors.add(yOffsetDescriptor);
        descriptors.add(colorProviderDescriptor);
        descriptors.add(frequencyDescriptor);
        descriptors.add(cellularReturnTypeDescriptor);
        descriptors.add(cellularDistanceDescriptor);
        descriptors.add(octaveDescriptor);
        descriptors.add(octaveCombinerDescriptor);
        descriptors.add(lacunarityDescriptor);
        descriptors.add(gainDescriptor);

        return descriptors;
    }

    private List<FractalTypeValueListElement> createFractalTypes() {
        return List.of(
                new FractalTypeValueListElement("perlin", (fastNoise, x, y) -> fastNoise.GetPerlin(x, y)),
                new FractalTypeValueListElement("perlinFractal", (fastNoise, x, y) -> fastNoise.GetPerlinFractal(x, y)),
                new FractalTypeValueListElement("cellular", (fastNoise, x, y) -> fastNoise.GetCellular(x, y)),
                new FractalTypeValueListElement("cubic", (fastNoise, x, y) -> fastNoise.GetCubic(x, y)),
                new FractalTypeValueListElement("cubicFractal", (fastNoise, x, y) -> fastNoise.GetCubicFractal(x, y)),
                new FractalTypeValueListElement("noise", (fastNoise, x, y) -> fastNoise.GetNoise(x, y)),
                new FractalTypeValueListElement("simplex", (fastNoise, x, y) -> fastNoise.GetSimplex(x, y)),
                new FractalTypeValueListElement("simplexFractal", (fastNoise, x, y) -> fastNoise.GetSimplexFractal(x, y)),
                new FractalTypeValueListElement("value", (fastNoise, x, y) -> fastNoise.GetValue(x, y)),
                new FractalTypeValueListElement("valueFractal", (fastNoise, x, y) -> fastNoise.GetValueFractal(x, y)),
                new FractalTypeValueListElement("whiteNoise", (fastNoise, x, y) -> fastNoise.GetWhiteNoise(x, y)));
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new NoiseProceduralClip(this, cloneRequestMetadata);
    }

    static class FractalTypeValueListElement extends ValueListElement {
        private FractalFunction fractalFunction;

        public FractalTypeValueListElement(String id, FractalFunction fractalFunction) {
            super(id, id);
            this.fractalFunction = fractalFunction;
        }

        public FractalFunction getFractalFunction() {
            return fractalFunction;
        }

    }

    static interface FractalFunction {
        float calculateFractalValue(FastNoise fastNoise, float x, float y);
    }

    static class CellularReturnTypeValueListElement extends ValueListElement {
        private CellularReturnType cellularReturnType;

        public CellularReturnTypeValueListElement(CellularReturnType cellularReturnType) {
            super(cellularReturnType.name(), cellularReturnType.name());
            this.cellularReturnType = cellularReturnType;
        }

        public CellularReturnType getCellularReturnType() {
            return cellularReturnType;
        }

    }

    static class FractalOctaveCombinerTypeValueListElement extends ValueListElement {
        private FractalType fractalType;

        public FractalOctaveCombinerTypeValueListElement(FractalType fractalType) {
            super(fractalType.name(), fractalType.name());
            this.fractalType = fractalType;
        }

        public FractalType getFractalType() {
            return fractalType;
        }

    }

    static class CellularDistanceFunctionValueListElement extends ValueListElement {
        private CellularDistanceFunction distanceFunction;

        public CellularDistanceFunctionValueListElement(CellularDistanceFunction distanceFunction) {
            super(distanceFunction.name(), distanceFunction.name());
            this.distanceFunction = distanceFunction;
        }

        public CellularDistanceFunction getCellularDistanceFunction() {
            return distanceFunction;
        }

    }
}
