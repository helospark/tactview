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
import com.helospark.tactview.core.timeline.proceduralclip.gradient.service.RadialGradientRequest;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.service.RadialGradientService;
import com.helospark.tactview.core.util.ReflectionUtil;

public class RadialGradientProceduralEffect extends ProceduralVisualClip {

    private RadialGradientService radialGradientService;

    private ColorProvider startColorProvider;
    private ColorProvider endColorProvider;

    private LineProvider lineProvider;
    private DoubleProvider innerSaturationDiameterProvider;

    public RadialGradientProceduralEffect(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, RadialGradientService radialGradientService) {
        super(visualMediaMetadata, interval);
        this.radialGradientService = radialGradientService;
    }

    public RadialGradientProceduralEffect(RadialGradientProceduralEffect gradientProceduralEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(gradientProceduralEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(gradientProceduralEffect, this);
    }

    public RadialGradientProceduralEffect(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, RadialGradientService radialGradientService) {
        super(metadata, node, loadMetadata);
        this.radialGradientService = radialGradientService;
    }

    @Override
    public ClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        InterpolationLine line = lineProvider.getValueAt(relativePosition);

        Point startPositionInPixels = line.start.multiply(request.getExpectedWidth(), request.getExpectedHeight());
        Point endPositionInPixels = line.end.multiply(request.getExpectedWidth(), request.getExpectedHeight());
        Point center = startPositionInPixels.center(endPositionInPixels);
        double radius = startPositionInPixels.distanceFrom(center);
        Color startColor = startColorProvider.getValueAt(relativePosition);
        Color endColor = endColorProvider.getValueAt(relativePosition);
        double innerSaturation = innerSaturationDiameterProvider.getValueAt(relativePosition);

        RadialGradientRequest gradientRequest = RadialGradientRequest.builder()
                .withCenter(center)
                .withEndColor(endColor)
                .withHeight(request.getExpectedHeight())
                .withInnerSaturation(innerSaturation)
                .withRadius(radius)
                .withStartColor(startColor)
                .withWidth(request.getExpectedWidth())
                .build();

        return radialGradientService.createImageWithGradient(gradientRequest);
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
