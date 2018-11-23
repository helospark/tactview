package com.helospark.tactview.core.timeline.proceduralclip.gradient;

import static com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE;

import java.util.List;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Line;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.IndependentPixelOperation;

public class GradientProceduralEffect extends ProceduralVisualClip {

    private IndependentPixelOperation independentPixelOperation;

    private ColorProvider startColorProvider;
    private ColorProvider endColorProvider;

    private ValueListProvider<ValueListElement> typeProvider;

    private LineProvider lineProvider;

    public GradientProceduralEffect(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(visualMediaMetadata, interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public GradientProceduralEffect(GradientProceduralEffect gradientProceduralEffect) {
        super(gradientProceduralEffect);
        this.independentPixelOperation = gradientProceduralEffect.independentPixelOperation;
    }

    @Override
    public ClipFrameResult createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipFrameResult result = ClipFrameResult.fromSize(request.getExpectedWidth(), request.getExpectedHeight());

        if (typeProvider.getValueAt(relativePosition).getId().equals("radial")) {
            Line line = lineProvider.getValueAt(relativePosition);

            Point startPositionInPixels = line.start.multiply(result.getWidth(), result.getHeight());
            Point endPositionInPixels = line.end.multiply(result.getWidth(), result.getHeight());
            Point center = startPositionInPixels.center(endPositionInPixels);
            double radius = startPositionInPixels.distanceFrom(center);
            Color startColor = startColorProvider.getValueAt(relativePosition);
            Color endColor = endColorProvider.getValueAt(relativePosition);

            independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
                double distance = center.distanceFrom(x, y);
                if (distance > radius) {
                    setColor(result, x, y, endColor);
                } else {
                    double factor = (distance / radius);
                    Color newColor = startColor.interpolate(endColor, factor);
                    setColor(result, x, y, newColor);
                }
            });
        }
        return result;
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
        typeProvider = createTypeProvider();

        PointProvider topLeftPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.3), doubleProviderWithDefaultValue(0.3));
        PointProvider bottomRightPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.6), doubleProviderWithDefaultValue(0.6));

        lineProvider = new LineProvider(topLeftPointProvider, bottomRightPointProvider);

        ValueProviderDescriptor startColorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(startColorProvider)
                .withName("Start color")
                .build();
        ValueProviderDescriptor endColorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(endColorProvider)
                .withName("End color")
                .build();
        ValueProviderDescriptor typeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(typeProvider)
                .withName("type")
                .build();
        ValueProviderDescriptor lineProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineProvider)
                .withName("Postion")
                .build();

        result.addAll(List.of(startColorDescriptor, endColorDescriptor, typeProviderDescriptor, lineProviderDescriptor));
        return result;
    }

    private DoubleProvider doubleProviderWithDefaultValue(double defaultValue) {
        return new DoubleProvider(IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(defaultValue));
    }

    private ValueListProvider<ValueListElement> createTypeProvider() {
        ValueListElement linear = new ValueListElement("linear", "linear");
        ValueListElement radial = new ValueListElement("radial", "radial");
        return new ValueListProvider<>(List.of(linear, radial), new StringInterpolator("radial"));
    }

    private ColorProvider createColorProvider(double r, double g, double b) {
        return new ColorProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(r)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(g)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(b)));
    }

    @Override
    public TimelineClip cloneClip() {
        return new GradientProceduralEffect(this);
    }

}
