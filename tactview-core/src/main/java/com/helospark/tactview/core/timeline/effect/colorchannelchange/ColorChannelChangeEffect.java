package com.helospark.tactview.core.timeline.effect.colorchannelchange;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class ColorChannelChangeEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private ValueListProvider<ColorElement> redProvider;
    private ValueListProvider<ColorElement> greenProvider;
    private ValueListProvider<ColorElement> blueProvider;
    private ValueListProvider<ColorElement> alphaProvider;

    public ColorChannelChangeEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public ColorChannelChangeEffect(ColorChannelChangeEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public ColorChannelChangeEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ColorElementOperation redChannelElement = redProvider.getValueAt(request.getEffectPosition()).getOperation();
        ColorElementOperation greenChannelElement = greenProvider.getValueAt(request.getEffectPosition()).getOperation();
        ColorElementOperation blueChannelElement = blueProvider.getValueAt(request.getEffectPosition()).getOperation();
        ColorElementOperation alphaChannelElement = alphaProvider.getValueAt(request.getEffectPosition()).getOperation();
        return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelRequest -> {
            pixelRequest.output[0] = redChannelElement.function.apply(pixelRequest.input);
            pixelRequest.output[1] = greenChannelElement.function.apply(pixelRequest.input);
            pixelRequest.output[2] = blueChannelElement.function.apply(pixelRequest.input);
            pixelRequest.output[3] = alphaChannelElement.function.apply(pixelRequest.input);
        });
    }

    @Override
    public void initializeValueProvider() {
        redProvider = new ValueListProvider<>(createColorOperations(), new StepStringInterpolator(ColorElementOperation.RED.id));
        greenProvider = new ValueListProvider<>(createColorOperations(), new StepStringInterpolator(ColorElementOperation.GREEN.id));
        blueProvider = new ValueListProvider<>(createColorOperations(), new StepStringInterpolator(ColorElementOperation.BLUE.id));
        alphaProvider = new ValueListProvider<>(createColorOperations(), new StepStringInterpolator(ColorElementOperation.ALPHA.id));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {

        ValueProviderDescriptor channel1ProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(redProvider)
                .withName("channel 1")
                .build();
        ValueProviderDescriptor channel2ProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(greenProvider)
                .withName("channel 2")
                .build();
        ValueProviderDescriptor channel3ProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(blueProvider)
                .withName("channel 3")
                .build();
        ValueProviderDescriptor channel4ProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(alphaProvider)
                .withName("channel 4")
                .build();

        return List.of(channel1ProviderDescriptor, channel2ProviderDescriptor, channel3ProviderDescriptor, channel4ProviderDescriptor);
    }

    private List<ColorElement> createColorOperations() {
        return Arrays.stream(ColorElementOperation.values())
                .map(a -> new ColorElement(a))
                .collect(Collectors.toList());
    }

    static class ColorElement extends ValueListElement {
        ColorElementOperation operation;

        public ColorElement(ColorElementOperation operation) {
            super(operation.id, operation.id);
            this.operation = operation;
        }

        public ColorElementOperation getOperation() {
            return operation;
        }

    }

    static enum ColorElementOperation {
        RED("red", colors -> colors[0]),
        GREEN("green", colors -> colors[1]),
        BLUE("blue", colors -> colors[2]),
        ALPHA("alpha", colors -> colors[3]),
        FULLY_ON("fully on", colors -> 255),
        FULLY_OFF("fully off", colors -> 0);

        String id;
        Function<int[], Integer> function;

        private ColorElementOperation(String id, Function<int[], Integer> function) {
            this.id = id;
            this.function = function;
        }

    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new ColorChannelChangeEffect(this, cloneRequestMetadata);
    }

}
