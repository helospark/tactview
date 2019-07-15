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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.ReflectionUtil;

public class GridProceduralClip extends ProceduralVisualClip {
    private DoubleProvider lineWidthProvider;
    private ColorProvider colorProvider;

    private DoubleProvider xDistanceProvider;
    private DoubleProvider yDistanceProvider;

    private DoubleProvider xOffsetProvider;
    private DoubleProvider yOffsetProvider;

    public GridProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval) {
        super(visualMediaMetadata, interval);
    }

    public GridProceduralClip(GridProceduralClip gridProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(gridProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(gridProceduralClip, this);
    }

    public GridProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata) {
        super(metadata, node, loadMetadata);
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());

        Color color = colorProvider.getValueAt(relativePosition).multiplyComponents(255.0);
        int width = (int) (lineWidthProvider.getValueAt(relativePosition) * request.getExpectedWidth());
        int xDistance = (int) (xDistanceProvider.getValueAt(relativePosition) * request.getExpectedWidth());
        int yDistance = (int) (yDistanceProvider.getValueAt(relativePosition) * request.getExpectedHeight());

        if (width <= 0) {
            width = 1;
        }
        if (xDistance <= 0) {
            xDistance = 1;
        }
        if (yDistance <= 0) {
            yDistance = 1;
        }

        int xOffset = (int) (xOffsetProvider.getValueAt(relativePosition) * request.getExpectedWidth()) % xDistance;
        int yOffset = (int) (yOffsetProvider.getValueAt(relativePosition) * request.getExpectedHeight()) % yDistance;

        int x = xOffset;
        while (x <= result.getWidth()) {

            drawSimpleVerticalLine(result, x, width, color);

            x += xDistance;
        }

        int y = yOffset;
        while (y <= result.getHeight()) {

            drawSimpleHorizontalLine(result, y, width, color);

            y += yDistance;
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
        return new GridProceduralClip(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        lineWidthProvider = new DoubleProvider(0.0001, 0.2, new MultiKeyframeBasedDoubleInterpolator(0.01));
        colorProvider = ColorProvider.fromDefaultValue(0.5, 0.5, 0.5);

        xDistanceProvider = new DoubleProvider(0.01, 2.0, new MultiKeyframeBasedDoubleInterpolator(0.1));
        yDistanceProvider = new DoubleProvider(0.01, 2.0, new MultiKeyframeBasedDoubleInterpolator(0.1));

        xOffsetProvider = new DoubleProvider(-2.0, 2.0, new MultiKeyframeBasedDoubleInterpolator(0.0));
        yOffsetProvider = new DoubleProvider(-2.0, 2.0, new MultiKeyframeBasedDoubleInterpolator(0.0));
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

        ValueProviderDescriptor xDistanceProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(xDistanceProvider)
                .withName("X distance")
                .build();

        ValueProviderDescriptor yDistanceProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(yDistanceProvider)
                .withName("Y distance")
                .build();

        ValueProviderDescriptor xOffsetProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(xOffsetProvider)
                .withName("X offset")
                .build();

        ValueProviderDescriptor yOffsetProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(yOffsetProvider)
                .withName("Y offset")
                .build();

        result.add(lineWidthProviderDescriptor);
        result.add(colorProviderDescriptor);

        result.add(xDistanceProviderDescriptor);
        result.add(yDistanceProviderDescriptor);

        result.add(xOffsetProviderDescriptor);
        result.add(yOffsetProviderDescriptor);

        return result;
    }

}
