package com.helospark.tactview.core.timeline.proceduralclip.gradient;

import static com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.ClipFrameResult;
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
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class LinearGradientProceduralEffect extends ProceduralVisualClip {

    private IndependentPixelOperation independentPixelOperation;

    private ColorProvider startColorProvider;
    private ColorProvider endColorProvider;

    private LineProvider lineProvider;

    public LinearGradientProceduralEffect(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(visualMediaMetadata, interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public LinearGradientProceduralEffect(LinearGradientProceduralEffect linearProceduralEffect) {
        super(linearProceduralEffect);
        ReflectionUtil.copyOrCloneFieldFromTo(linearProceduralEffect, this);
    }

    @Override
    public ClipFrameResult createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipFrameResult result = ClipFrameResult.fromSize(request.getExpectedWidth(), request.getExpectedHeight());

        InterpolationLine line = lineProvider.getValueAt(relativePosition);

        Point startPositionInPixels = line.start.multiply(result.getWidth(), result.getHeight());
        Point endPositionInPixels = line.end.multiply(result.getWidth(), result.getHeight());

        Vector2D start = new Vector2D(startPositionInPixels.x, startPositionInPixels.y);
        Vector2D originalEnd = new Vector2D(endPositionInPixels.x, endPositionInPixels.y);
        double lineDistance = start.distance(originalEnd);

        Line perpendicularLine = getPerpendicularLine(start, originalEnd);

        Color startColor = startColorProvider.getValueAt(relativePosition);
        Color endColor = endColorProvider.getValueAt(relativePosition);

        independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
            double pixelDistance = perpendicularLine.distance(new Vector2D(x, y)); // TODO: avoid new on every pixel

            if (pixelDistance > lineDistance) {
                setColor(result, x, y, endColor);
            } else {
                double factor = pixelDistance / lineDistance;
                Color newColor = startColor.interpolate(endColor, factor);
                setColor(result, x, y, newColor);
            }

        });
        return result;
    }

    private Line getPerpendicularLine(Vector2D start, Vector2D originalEnd) {
        Vector2D direction = originalEnd.subtract(start);
        Vector2D perpendicularDirection = new Vector2D(direction.getY(), -direction.getX()).normalize();
        Vector2D end = start.add(perpendicularDirection);

        Line perpendicularLine = new Line(start, end, 0.0001);
        return perpendicularLine;
    }

    private void setColor(ClipFrameResult result, Integer x, Integer y, Color endColor) {
        result.setRed((int) (endColor.red * 255), x, y);
        result.setGreen((int) (endColor.green * 255), x, y);
        result.setBlue((int) (endColor.blue * 255), x, y);
        result.setAlpha(255, x, y);
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        startColorProvider = createColorProvider(0.0, 0.0, 0.0);
        endColorProvider = createColorProvider(1.0, 1.0, 1.0);

        PointProvider topLeftPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.0), doubleProviderWithDefaultValue(0.0));
        PointProvider bottomRightPointProvider = new PointProvider(doubleProviderWithDefaultValue(1.0), doubleProviderWithDefaultValue(0.0));

        lineProvider = new LineProvider(topLeftPointProvider, bottomRightPointProvider);

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

        result.addAll(List.of(startColorDescriptor, endColorDescriptor, lineProviderDescriptor));
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
    public TimelineClip cloneClip() {
        return new LinearGradientProceduralEffect(this);
    }

}
