package com.helospark.tactview.core.timeline.effect.levels;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.DoubleRange;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleRangeProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

// Some logic adapter from Gimp: https://github.com/GNOME/gimp/blob/e09e563a70fef5d7dd55e5e8d0e280348f1ef9d4/app/operations/gimpoperationlevels.c
public class LevelsEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private DoubleRangeProvider fromValueRangeProvider;
    private DoubleRangeProvider fromRedRangeProvider;
    private DoubleRangeProvider fromGreenRangeProvider;
    private DoubleRangeProvider fromBlueRangeProvider;

    private DoubleRangeProvider toValueRangeProvider;
    private DoubleRangeProvider toRedRangeProvider;
    private DoubleRangeProvider toGreenRangeProvider;
    private DoubleRangeProvider toBlueRangeProvider;

    public LevelsEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public LevelsEffect(LevelsEffect levelsEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(levelsEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(levelsEffect, this);
    }

    public LevelsEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        DoubleRange fromValueRange = fromValueRangeProvider.getValueAt(request.getEffectPosition());
        DoubleRange fromRedRange = fromRedRangeProvider.getValueAt(request.getEffectPosition());
        DoubleRange fromGreenRange = fromGreenRangeProvider.getValueAt(request.getEffectPosition());
        DoubleRange fromBlueRange = fromBlueRangeProvider.getValueAt(request.getEffectPosition());

        DoubleRange toValueRange = toValueRangeProvider.getValueAt(request.getEffectPosition());
        DoubleRange toRedRange = toRedRangeProvider.getValueAt(request.getEffectPosition());
        DoubleRange toGreenRange = toGreenRangeProvider.getValueAt(request.getEffectPosition());
        DoubleRange toBlueRange = toBlueRangeProvider.getValueAt(request.getEffectPosition());

        return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelRequest -> {
            pixelRequest.output[0] = mapInputByRanges(pixelRequest.input[0], fromRedRange, toRedRange, fromValueRange, toValueRange);
            pixelRequest.output[1] = mapInputByRanges(pixelRequest.input[1], fromGreenRange, toGreenRange, fromValueRange, toValueRange);
            pixelRequest.output[2] = mapInputByRanges(pixelRequest.input[2], fromBlueRange, toBlueRange, fromValueRange, toValueRange);
            pixelRequest.output[3] = pixelRequest.input[3];
        });

    }

    private int mapInputByRanges(int input, DoubleRange fromColorRange, DoubleRange toColorRange, DoubleRange fromValueRange, DoubleRange toValueRange) {
        double value = mapInputWithRange(input / 255.0, fromColorRange.lowEnd, fromColorRange.highEnd, toColorRange.lowEnd, toColorRange.highEnd);
        return (int) (mapInputWithRange(value, fromValueRange.lowEnd, fromValueRange.highEnd, toValueRange.lowEnd, toValueRange.highEnd) * 255.0);
    }

    private double mapInputWithRange(double value, double low_input, double high_input, double low_output, double high_output) {
        if (high_input != low_input)
            value = (value - low_input) / (high_input - low_input);
        else
            value = (value - low_input);

        if (high_output >= low_output)
            value = value * (high_output - low_output) + low_output;
        else if (high_output < low_output)
            value = low_output - value * (low_output - high_output);

        return value;
    }

    @Override
    public void initializeValueProvider() {
        fromValueRangeProvider = DoubleRangeProvider.createDefaultDoubleRangeProvider(0.0, 1.0, 0.0, 1.0);
        fromRedRangeProvider = DoubleRangeProvider.createDefaultDoubleRangeProvider(0.0, 1.0, 0.0, 1.0);
        fromGreenRangeProvider = DoubleRangeProvider.createDefaultDoubleRangeProvider(0.0, 1.0, 0.0, 1.0);
        fromBlueRangeProvider = DoubleRangeProvider.createDefaultDoubleRangeProvider(0.0, 1.0, 0.0, 1.0);

        toValueRangeProvider = DoubleRangeProvider.createDefaultDoubleRangeProvider(0.0, 1.0, 0.0, 1.0);
        toRedRangeProvider = DoubleRangeProvider.createDefaultDoubleRangeProvider(0.0, 1.0, 0.0, 1.0);
        toGreenRangeProvider = DoubleRangeProvider.createDefaultDoubleRangeProvider(0.0, 1.0, 0.0, 1.0);
        toBlueRangeProvider = DoubleRangeProvider.createDefaultDoubleRangeProvider(0.0, 1.0, 0.0, 1.0);
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        // TODO: grouping
        ValueProviderDescriptor fromValueDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fromValueRangeProvider)
                .withName("value from")
                .build();
        ValueProviderDescriptor fromRedDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fromRedRangeProvider)
                .withName("red from")
                .build();
        ValueProviderDescriptor fromGreenDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fromGreenRangeProvider)
                .withName("green from")
                .build();
        ValueProviderDescriptor fromBlueDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fromBlueRangeProvider)
                .withName("blue from")
                .build();

        ValueProviderDescriptor toValueDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(toValueRangeProvider)
                .withName("value to")
                .build();
        ValueProviderDescriptor toRedDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(toRedRangeProvider)
                .withName("red to")
                .build();
        ValueProviderDescriptor toGreenDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(toGreenRangeProvider)
                .withName("green to")
                .build();
        ValueProviderDescriptor toBlueDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(toBlueRangeProvider)
                .withName("blue to")
                .build();

        return List.of(fromValueDescriptor, toValueDescriptor, fromRedDescriptor, toRedDescriptor, fromGreenDescriptor, toGreenDescriptor, fromBlueDescriptor, toBlueDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new LevelsEffect(this, cloneRequestMetadata);
    }

}
