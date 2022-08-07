package com.helospark.tactview.core.timeline.proceduralclip.polygon;

import static com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.GetPositionParameters;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier.BezierDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.core.util.ReflectionUtil;

public class RectangleProceduralClip extends ProceduralVisualClip {
    private static final String GRADIENT_ID = "gradient";
    private static final String COLOR_ID = "color";

    private IndependentPixelOperation independentPixelOperation;

    private ValueListProvider<ValueListElement> fillTypeProvider;

    private ColorProvider colorProvider;
    private LineProvider lineProvider;
    private DoubleProvider fuzzyDistanceProvider;

    private ColorProvider startColorGradientProvider;
    private ColorProvider endColorGradientProvider;
    private BooleanProvider horizontalGradientProvider;
    private BooleanProvider extendedFrameProvider;

    public RectangleProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(visualMediaMetadata, interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public RectangleProceduralClip(RectangleProceduralClip gradientProceduralEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(gradientProceduralEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(gradientProceduralEffect, this, cloneRequestMetadata);
    }

    public RectangleProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(metadata, node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        InterpolationLine line = lineProvider.getValueAt(relativePosition, request.getEvaluationContext());
        Point startPositionNormalized = line.start;
        Point endPositionNormalized = line.end;

        boolean isExtendedFrame = extendedFrameProvider.getValueAt(relativePosition, request.getEvaluationContext());

        if (isExtendedFrame) {
            return extendedFrameDraw(request, relativePosition, startPositionNormalized, endPositionNormalized);
        } else {
            return nonExtendedFrameDraw(request, relativePosition, startPositionNormalized, endPositionNormalized);
        }
    }

    private ClipImage nonExtendedFrameDraw(GetFrameRequest request, TimelinePosition relativePosition, Point startPositionNormalized, Point endPositionNormalized) {
        ClipImage result = ClipImage.fromSize((int) (Math.abs(endPositionNormalized.x - startPositionNormalized.x) * request.getExpectedWidth()),
                (int) (Math.abs(endPositionNormalized.y - startPositionNormalized.y) * request.getExpectedHeight()));

        String fillType = fillTypeProvider.getValueAt(relativePosition, request.getEvaluationContext()).getId();
        Color globalPixelColor = colorProvider.getValueAt(relativePosition, request.getEvaluationContext()).multiplyComponents(255.0);
        Color startGradientColor = startColorGradientProvider.getValueAt(relativePosition, request.getEvaluationContext()).multiplyComponents(255.0);
        Color endGradientColor = endColorGradientProvider.getValueAt(relativePosition, request.getEvaluationContext()).multiplyComponents(255.0);
        boolean horizontal = horizontalGradientProvider.getValueAt(relativePosition, request.getEvaluationContext());

        double fuzzyDistance = fuzzyDistanceProvider.getValueAt(relativePosition, request.getEvaluationContext());

        double fuzzyDistancePixel = fuzzyDistance * Math.min(result.getWidth(), result.getHeight());

        independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
            double normalizedX = (double) x / result.getWidth();
            double normalizedY = (double) y / result.getHeight();

            double alphaNormalized = 1.0;
            if (fuzzyDistance > 0) {
                double minLeftXDistance = Math.abs(x);
                double minRightXDistance = Math.abs(result.getWidth() - x);
                double maxTopYDistance = Math.abs(y);
                double maxBottomYDistance = Math.abs(result.getHeight() - y);

                double minDistance = MathUtil.min(minLeftXDistance, minRightXDistance, maxTopYDistance, maxBottomYDistance);

                if (minDistance < fuzzyDistancePixel) {
                    alphaNormalized = Math.min(minDistance / fuzzyDistancePixel, 1.0);
                }
            }
            Color color = globalPixelColor;
            if (fillType.equals(GRADIENT_ID)) {
                double percent = horizontal ? normalizedX : normalizedY;
                color = startGradientColor.interpolate(endGradientColor, percent);
            }

            result.setRed((int) color.red, x, y);
            result.setGreen((int) color.green, x, y);
            result.setBlue((int) color.blue, x, y);
            result.setAlpha((int) (alphaNormalized * 255.0), x, y);
        });
        return result;
    }

    private ClipImage extendedFrameDraw(GetFrameRequest request, TimelinePosition relativePosition, Point startPositionNormalized, Point endPositionNormalized) {
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());

        String fillType = fillTypeProvider.getValueAt(relativePosition, request.getEvaluationContext()).getId();
        Color globalPixelColor = colorProvider.getValueAt(relativePosition, request.getEvaluationContext()).multiplyComponents(255.0);
        Color startGradientColor = startColorGradientProvider.getValueAt(relativePosition, request.getEvaluationContext()).multiplyComponents(255.0);
        Color endGradientColor = endColorGradientProvider.getValueAt(relativePosition, request.getEvaluationContext()).multiplyComponents(255.0);
        boolean horizontal = horizontalGradientProvider.getValueAt(relativePosition, request.getEvaluationContext());

        double fuzzyDistance = fuzzyDistanceProvider.getValueAt(relativePosition, request.getEvaluationContext());

        double fuzzyDistancePixel = fuzzyDistance * Math.min(result.getWidth(), result.getHeight());

        Point startPosition = startPositionNormalized.multiply(request.getExpectedWidth(), request.getExpectedHeight());
        Point endPosition = endPositionNormalized.multiply(request.getExpectedWidth(), request.getExpectedHeight());

        independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
            if (x >= startPosition.x && x <= endPosition.x && y >= startPosition.y && y <= endPosition.y) {
                double rectangleX = x - startPosition.x;
                double rectangleY = y - startPosition.y;

                double normalizedX = rectangleX / (endPosition.x - startPosition.x);
                double normalizedY = rectangleY / (endPosition.y - startPosition.y);

                double alphaNormalized = 1.0;
                if (fuzzyDistance > 0) {
                    double minLeftXDistance = Math.abs(rectangleX);
                    double minRightXDistance = Math.abs(endPosition.x - x);
                    double maxTopYDistance = Math.abs(rectangleY);
                    double maxBottomYDistance = Math.abs(endPosition.y - y);

                    double minDistance = MathUtil.min(minLeftXDistance, minRightXDistance, maxTopYDistance, maxBottomYDistance);

                    if (minDistance < fuzzyDistancePixel) {
                        alphaNormalized = Math.min(minDistance / fuzzyDistancePixel, 1.0);
                    }
                }
                Color color = globalPixelColor;
                if (fillType.equals(GRADIENT_ID)) {
                    double percent = horizontal ? normalizedX : normalizedY;
                    color = startGradientColor.interpolate(endGradientColor, percent);
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

        fillTypeProvider = new ValueListProvider<>(createFillTypeElements(), new StepStringInterpolator(COLOR_ID));

        colorProvider = ColorProvider.fromDefaultValue(0.5, 0.5, 0.5);

        startColorGradientProvider = ColorProvider.fromDefaultValue(0.2, 0.2, 0.2);
        endColorGradientProvider = ColorProvider.fromDefaultValue(1.0, 1.0, 1.0);
        horizontalGradientProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));

        fuzzyDistanceProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.0));

        PointProvider topLeftPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.3), doubleProviderWithDefaultValue(0.3));
        PointProvider bottomRightPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.6), doubleProviderWithDefaultValue(0.6));
        lineProvider = new LineProvider(topLeftPointProvider, bottomRightPointProvider);

        extendedFrameProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));
    }

    private List<ValueListElement> createFillTypeElements() {
        return List.of(
                new ValueListElement(COLOR_ID, COLOR_ID),
                new ValueListElement(GRADIENT_ID, GRADIENT_ID));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor fillTypeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fillTypeProvider)
                .withName("Fill")
                .withGroup("fill")
                .build();

        ValueProviderDescriptor colorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorProvider)
                .withName("Color")
                .withGroup("fill")
                .withShowPredicate(position -> fillTypeProvider.getValueWithoutScriptAt(position).getId().equals(COLOR_ID))
                .build();

        ValueProviderDescriptor startColorGradientDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(startColorGradientProvider)
                .withName("Start color")
                .withGroup("fill")
                .withShowPredicate(position -> fillTypeProvider.getValueWithoutScriptAt(position).getId().equals(GRADIENT_ID))
                .build();
        ValueProviderDescriptor endColorGradientDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(endColorGradientProvider)
                .withName("End color")
                .withGroup("fill")
                .withShowPredicate(position -> fillTypeProvider.getValueWithoutScriptAt(position).getId().equals(GRADIENT_ID))
                .build();
        ValueProviderDescriptor horizontalGradientProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(horizontalGradientProvider)
                .withName("Horizontal")
                .withGroup("fill")
                .withShowPredicate(position -> fillTypeProvider.getValueWithoutScriptAt(position).getId().equals(GRADIENT_ID))
                .build();

        ValueProviderDescriptor lineProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineProvider)
                .withName("Area")
                .build();
        ValueProviderDescriptor innerSaturationDiameterProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fuzzyDistanceProvider)
                .withName("Fuzziness")
                .build();
        ValueProviderDescriptor extendedFrameProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(extendedFrameProvider)
                .withName("Extended frame")
                .build();

        result.addAll(List.of(extendedFrameProviderDescriptor, fillTypeDescriptor, colorDescriptor, startColorGradientDescriptor, endColorGradientDescriptor, horizontalGradientProviderDescriptor,
                lineProviderDescriptor, innerSaturationDiameterProviderDescriptor));
        return result;
    }

    private DoubleProvider doubleProviderWithDefaultValue(double defaultValue) {
        return new DoubleProvider(IMAGE_SIZE_IN_0_to_1_RANGE, new BezierDoubleInterpolator(defaultValue));
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new RectangleProceduralClip(this, cloneRequestMetadata);
    }

    @Override
    public int getXPosition(GetPositionParameters parameterObject) {
        if (extendedFrameProvider.getValueAt(parameterObject.getTimelinePosition(), parameterObject.getEvaluationContext())) {
            return super.getXPosition(parameterObject);
        } else {
            InterpolationLine line = lineProvider.getValueAt(parameterObject.getTimelinePosition(), parameterObject.getEvaluationContext());
            double xPos = Math.min(line.start.x, line.end.x);
            return (int) (xPos * parameterObject.getWidth()) + super.getXPosition(parameterObject);
        }
    }

    @Override
    public int getYPosition(GetPositionParameters parameterObject) {
        if (extendedFrameProvider.getValueAt(parameterObject.getTimelinePosition(), parameterObject.getEvaluationContext())) {
            return super.getYPosition(parameterObject);
        } else {
            InterpolationLine line = lineProvider.getValueAt(parameterObject.getTimelinePosition(), parameterObject.getEvaluationContext());
            double yPos = Math.min(line.start.y, line.end.y);
            return (int) (yPos * parameterObject.getHeight()) + super.getYPosition(parameterObject);
        }
    }
}
