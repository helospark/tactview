package com.helospark.tactview.core.timeline.proceduralclip.highlight;

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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Rectangle;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.FileProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.RectangleProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.timeline.proceduralclip.lines.impl.DrawLineRequest;
import com.helospark.tactview.core.timeline.proceduralclip.lines.impl.DrawLineService;
import com.helospark.tactview.core.util.BresenhemPixelProvider;
import com.helospark.tactview.core.util.ReflectionUtil;

public class DrawnRectangleHighlightProceduralEffect extends ProceduralVisualClip {
    private RectangleProvider rectangleProvider;
    private IntegerProvider brushSizeProvider;
    private DoubleProvider endPositionProvider;
    private DoubleProvider overshootProvider;
    private ColorProvider colorProvider;
    private FileProvider brushFileProvider;

    private DrawLineService drawLineService;
    private BresenhemPixelProvider bresenhemPixelProvider;

    public DrawnRectangleHighlightProceduralEffect(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider) {
        super(visualMediaMetadata, interval);
        this.drawLineService = drawLineService;
        this.bresenhemPixelProvider = bresenhemPixelProvider;

    }

    public DrawnRectangleHighlightProceduralEffect(DrawnRectangleHighlightProceduralEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public DrawnRectangleHighlightProceduralEffect(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider2) {
        super(metadata, node, loadMetadata);
        this.drawLineService = drawLineService;
        this.bresenhemPixelProvider = bresenhemPixelProvider2;
    }

    @Override
    public ClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
        double progress;

        double endSeconds = endPositionProvider.getValueAt(relativePosition);
        double actualSeconds = relativePosition.getSeconds().doubleValue();
        if (endSeconds > actualSeconds) {
            progress = actualSeconds / endSeconds;
        } else {
            progress = 1.0;
        }

        Rectangle rectangle = rectangleProvider.getValueAt(relativePosition);

        double overshoot = overshootProvider.getValueAt(relativePosition);
        double totalLength = rectangle.getLength() * (1.0 + overshoot * 2 * 4);

        double lengthToDraw = progress * totalLength;

        int brushSize = (int) (brushSizeProvider.getValueAt(relativePosition) * request.getScale());
        if (brushSize < 1) {
            brushSize = 1;
        }

        String brushFilePath = brushFileProvider.getValueOrDefault(relativePosition, "classpath:/brushes/Sponge-02.gbr");

        Color color = colorProvider.getValueAt(relativePosition);

        for (int i = 0; i < 4 && lengthToDraw > 0.0; ++i) { // 0->1, 1->2, 2->3, 3->0
            Point start = rectangle.points.get(i);
            Point end = rectangle.points.get((i + 1) % 4);

            Point overshootOffsetVector = end.subtract(start).normalize().scalarMultiply(overshoot);

            start = start.subtract(overshootOffsetVector);
            end = end.add(overshootOffsetVector);

            double distance = start.distanceFrom(end);
            double lineProgress = lengthToDraw > distance ? 1.0 : lengthToDraw / distance;

            Point startInPixels = start.multiply(request.getExpectedWidth(), request.getExpectedHeight());
            Point endInPixels = end.multiply(request.getExpectedWidth(), request.getExpectedHeight());
            DrawLineRequest drawLineRequest = DrawLineRequest.builder()
                    .withBrushFilePath(brushFilePath)
                    .withBrushSize(brushSize)
                    .withColor(color)
                    .withPixels(bresenhemPixelProvider.linePixels(startInPixels, endInPixels))
                    .withProgress(lineProgress)
                    .withResult(result)
                    .build();

            drawLineService.drawLine(drawLineRequest);

            lengthToDraw -= distance;
        }

        return result;
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        rectangleProvider = RectangleProvider.createDefaultFullImageWithNormalizedCenterAndSize(new Point(0.5, 0.5), 0.1, 0.1);
        colorProvider = ColorProvider.fromDefaultValue(0, 0, 0);
        brushSizeProvider = new IntegerProvider(1, 200, new MultiKeyframeBasedDoubleInterpolator(70.0));
        endPositionProvider = new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(2.0));
        overshootProvider = new DoubleProvider(0.0, 0.2, new MultiKeyframeBasedDoubleInterpolator(0.02));
        brushFileProvider = new FileProvider("gbr", new StepStringInterpolator());
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor areaProvider = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(rectangleProvider)
                .withName("Area")
                .build();
        ValueProviderDescriptor colorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorProvider)
                .withName("color")
                .build();
        ValueProviderDescriptor brushSizeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(brushSizeProvider)
                .withName("bursh size")
                .build();
        ValueProviderDescriptor endPositionProviderDesctiptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(endPositionProvider)
                .withName("animation length")
                .build();
        ValueProviderDescriptor brushProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(brushFileProvider)
                .withName("brush")
                .build();

        result.add(areaProvider);
        result.add(colorProviderDescriptor);
        result.add(brushSizeProviderDescriptor);
        result.add(endPositionProviderDesctiptor);
        result.add(brushProviderDescriptor);

        return result;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new DrawnRectangleHighlightProceduralEffect(this, cloneRequestMetadata);
    }

}
