package com.helospark.tactview.core.timeline.proceduralclip.gradient;

import static com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE;

import java.util.List;

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
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class RadialGradientProceduralEffect extends ProceduralVisualClip {

    private IndependentPixelOperation independentPixelOperation;

    private ColorProvider startColorProvider;
    private ColorProvider endColorProvider;

    private LineProvider lineProvider;
    private DoubleProvider innerSaturationDiameterProvider;

    public RadialGradientProceduralEffect(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(visualMediaMetadata, interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public RadialGradientProceduralEffect(RadialGradientProceduralEffect gradientProceduralEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(gradientProceduralEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(gradientProceduralEffect, this);
    }

    public RadialGradientProceduralEffect(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2) {
        super(metadata, node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    public ClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());

        InterpolationLine line = lineProvider.getValueAt(relativePosition);

        Point startPositionInPixels = line.start.multiply(result.getWidth(), result.getHeight());
        Point endPositionInPixels = line.end.multiply(result.getWidth(), result.getHeight());
        Point center = startPositionInPixels.center(endPositionInPixels);
        double radius = startPositionInPixels.distanceFrom(center);
        Color startColor = startColorProvider.getValueAt(relativePosition);
        Color endColor = endColorProvider.getValueAt(relativePosition);
        double innerSaturation = innerSaturationDiameterProvider.getValueAt(relativePosition);

        independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
            double distance = center.distanceFrom(x, y);
            if (distance > radius) {
                setColor(result, x, y, endColor);
            } else {
                double factor = (distance / radius);
                if (factor <= innerSaturation) {
                    setColor(result, x, y, startColor);
                } else {
                    double realDistanceNormalized = 1.0 - innerSaturation;
                    factor = (factor - innerSaturation) / realDistanceNormalized;
                    Color newColor = startColor.interpolate(endColor, factor);
                    setColor(result, x, y, newColor);
                }
            }
        });
        return result;
    }

    private void setColor(ClipImage result, Integer x, Integer y, Color endColor) {
        result.setRed((int) (endColor.red * 255), x, y);
        result.setGreen((int) (endColor.green * 255), x, y);
        result.setBlue((int) (endColor.blue * 255), x, y);
        result.setAlpha(255, x, y);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        startColorProvider = createColorProvider(0.0, 0.0, 0.0);
        endColorProvider = createColorProvider(1.0, 1.0, 1.0);
        innerSaturationDiameterProvider = new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));

        PointProvider topLeftPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.3), doubleProviderWithDefaultValue(0.3));
        PointProvider bottomRightPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.6), doubleProviderWithDefaultValue(0.6));
        lineProvider = new LineProvider(topLeftPointProvider, bottomRightPointProvider);

    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor startColorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(startColorProvider)
                .withName("Start color")
                .build();
        ValueProviderDescriptor endColorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(endColorProvider)
                .withName("End color")
                .build();
        ValueProviderDescriptor lineProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineProvider)
                .withName("Postion")
                .build();
        ValueProviderDescriptor innerSaturationDiameterProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(innerSaturationDiameterProvider)
                .withName("Inner saturation")
                .build();

        result.addAll(List.of(startColorDescriptor, endColorDescriptor, lineProviderDescriptor, innerSaturationDiameterProviderDescriptor));
        return result;
    }

    private DoubleProvider doubleProviderWithDefaultValue(double defaultValue) {
        return new DoubleProvider(IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(defaultValue));
    }

    private ColorProvider createColorProvider(double r, double g, double b) {
        return new ColorProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(r)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(g)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(b)));
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new RadialGradientProceduralEffect(this, cloneRequestMetadata);
    }

}
