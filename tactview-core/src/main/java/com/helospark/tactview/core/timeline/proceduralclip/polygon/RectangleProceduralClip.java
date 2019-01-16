package com.helospark.tactview.core.timeline.proceduralclip.polygon;

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
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.core.util.ReflectionUtil;

public class RectangleProceduralClip extends ProceduralVisualClip {
    private IndependentPixelOperation independentPixelOperation;

    private ColorProvider colorProvider;
    private LineProvider lineProvider;
    private DoubleProvider fuzzyDistanceProvider;

    public RectangleProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(visualMediaMetadata, interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public RectangleProceduralClip(RectangleProceduralClip gradientProceduralEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(gradientProceduralEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(gradientProceduralEffect, this);
    }

    public RectangleProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(metadata, node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());

        InterpolationLine line = lineProvider.getValueAt(relativePosition);

        Point startPositionInPixels = line.start;
        Point endPositionInPixels = line.end;

        Color color = colorProvider.getValueAt(relativePosition).multiplyComponents(255.0);
        double fuzzyDistance = fuzzyDistanceProvider.getValueAt(relativePosition);

        independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
            double normalizedX = (double) x / result.getWidth();
            double normalizedY = (double) y / result.getHeight();

            if (normalizedX >= startPositionInPixels.x && normalizedY >= startPositionInPixels.y
                    && normalizedX < endPositionInPixels.x && normalizedY < endPositionInPixels.y) {
                double alphaNormalized = 1.0;
                if (fuzzyDistance > 0) {
                    double minLeftXDistance = Math.abs(startPositionInPixels.x - normalizedX);
                    double minRightXDistance = Math.abs(endPositionInPixels.x - normalizedX);
                    double maxTopYDistance = Math.abs(startPositionInPixels.y - normalizedY);
                    double maxBottomYDistance = Math.abs(endPositionInPixels.y - normalizedY);

                    double minDistance = MathUtil.min(minLeftXDistance, minRightXDistance, maxTopYDistance, maxBottomYDistance);

                    alphaNormalized = Math.min(minDistance / fuzzyDistance, 1.0);
                }

                result.setRed((int) color.red, x, y);
                result.setGreen((int) color.green, x, y);
                result.setBlue((int) color.blue, x, y);
                result.setAlpha((int) (alphaNormalized * 255.0), x, y);
            }
        });
        return result;
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        colorProvider = ColorProvider.fromDefaultValue(0.5, 0.5, 0.5);
        fuzzyDistanceProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.0));

        PointProvider topLeftPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.3), doubleProviderWithDefaultValue(0.3));
        PointProvider bottomRightPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.6), doubleProviderWithDefaultValue(0.6));
        lineProvider = new LineProvider(topLeftPointProvider, bottomRightPointProvider);

    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor startColorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorProvider)
                .withName("Color")
                .build();
        ValueProviderDescriptor lineProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineProvider)
                .withName("Area")
                .build();
        ValueProviderDescriptor innerSaturationDiameterProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fuzzyDistanceProvider)
                .withName("Fuzziness")
                .build();

        result.addAll(List.of(startColorDescriptor, lineProviderDescriptor, innerSaturationDiameterProviderDescriptor));
        return result;
    }

    private DoubleProvider doubleProviderWithDefaultValue(double defaultValue) {
        return new DoubleProvider(IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(defaultValue));
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new RectangleProceduralClip(this, cloneRequestMetadata);
    }

}
