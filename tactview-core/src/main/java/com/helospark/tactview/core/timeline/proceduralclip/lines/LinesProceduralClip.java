package com.helospark.tactview.core.timeline.proceduralclip.lines;

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
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.ReflectionUtil;

public class LinesProceduralClip extends ProceduralVisualClip {
    private DoubleProvider lineWidthProvider;
    private ColorProvider colorProvider;

    private DoubleProvider distanceProvider;
    private DoubleProvider offsetProvider;

    private ValueListProvider<ValueListElement> orientationProvider;

    public LinesProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval) {
        super(visualMediaMetadata, interval);
    }

    public LinesProceduralClip(LinesProceduralClip gridProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(gridProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(gridProceduralClip, this, cloneRequestMetadata);
    }

    public LinesProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata) {
        super(metadata, node, loadMetadata);
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());

        Color color = colorProvider.getValueAt(relativePosition, request.getEvaluationContext()).multiplyComponents(255.0);
        int width = (int) (lineWidthProvider.getValueAt(relativePosition, request.getEvaluationContext()) * request.getExpectedWidth());
        int distance = (int) (distanceProvider.getValueAt(relativePosition, request.getEvaluationContext()) * request.getExpectedWidth());

        if (width <= 0) {
            width = 1;
        }
        if (distance <= 0) {
            distance = 1;
        }

        int offset = (int) (offsetProvider.getValueAt(relativePosition, request.getEvaluationContext()) * request.getExpectedWidth()) % distance;

        int x = offset;

        String orientation = orientationProvider.getValueAt(relativePosition, request.getEvaluationContext()).getId();

        if (orientation.equals("horizontal")) {
            while (x <= result.getWidth()) {

                drawSimpleVerticalLine(result, x, width, color);

                x += distance;
            }
        } else {
            int y = offset;
            while (y <= result.getHeight()) {

                drawSimpleHorizontalLine(result, y, width, color);

                y += distance;
            }

        }

        return result;
    }

    private void drawSimpleVerticalLine(ClipImage result, int x, int width, Color color) {
        int halfWidth = width / 2;
        for (int i = 0; i < result.getHeight(); ++i) {
            for (int j = -halfWidth; j <= halfWidth; ++j) {
                int currentX = x + j;
                int currentY = i;

                setColor(result, color, currentX, currentY);
            }
        }
    }

    private void setColor(ClipImage result, Color color, int currentX, int currentY) {
        if (result.inBounds(currentX, currentY)) {
            result.setRed((int) color.red, currentX, currentY);
            result.setGreen((int) color.green, currentX, currentY);
            result.setBlue((int) color.blue, currentX, currentY);
            result.setAlpha(255, currentX, currentY);
        }
    }

    private void drawSimpleHorizontalLine(ClipImage result, int y, int width, Color color) {
        int halfWidth = width / 2;
        for (int i = 0; i < result.getWidth(); ++i) {
            for (int j = -halfWidth; j <= halfWidth; ++j) {
                int currentY = y + j;
                int currentX = i;

                setColor(result, color, currentX, currentY);
            }
        }
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new LinesProceduralClip(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        lineWidthProvider = new DoubleProvider(0.0001, 0.2, new MultiKeyframeBasedDoubleInterpolator(0.01));
        colorProvider = ColorProvider.fromDefaultValue(0.5, 0.5, 0.5);

        distanceProvider = new DoubleProvider(0.001, 2.0, new MultiKeyframeBasedDoubleInterpolator(0.1));
        offsetProvider = new DoubleProvider(-2.0, 2.0, new MultiKeyframeBasedDoubleInterpolator(0.0));
        orientationProvider = new ValueListProvider<>(createOrientationElements(), new StepStringInterpolator("vertical"));
    }

    private List<ValueListElement> createOrientationElements() {
        return List.of(new ValueListElement("vertical", "Vertical"),
                new ValueListElement("horizontal", "Horizontal"));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor lineWidthProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineWidthProvider)
                .withName("Line width")
                .build();

        ValueProviderDescriptor colorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorProvider)
                .withName("Color")
                .build();

        ValueProviderDescriptor distanceProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(distanceProvider)
                .withName("distance")
                .build();

        ValueProviderDescriptor offsetProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(offsetProvider)
                .withName("offset")
                .build();

        ValueProviderDescriptor orientationDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(orientationProvider)
                .withName("orientation")
                .build();

        result.add(lineWidthProviderDescriptor);
        result.add(colorProviderDescriptor);

        result.add(distanceProviderDescriptor);

        result.add(offsetProviderDescriptor);
        result.add(orientationDescriptor);

        return result;
    }

}
