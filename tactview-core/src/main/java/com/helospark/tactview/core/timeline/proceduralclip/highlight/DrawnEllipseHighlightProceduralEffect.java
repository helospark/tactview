package com.helospark.tactview.core.timeline.proceduralclip.highlight;

import static com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.FileProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.timeline.proceduralclip.lines.impl.DrawLineRequest;
import com.helospark.tactview.core.timeline.proceduralclip.lines.impl.DrawLineService;
import com.helospark.tactview.core.util.BresenhemPixelProvider;

public class DrawnEllipseHighlightProceduralEffect extends ProceduralVisualClip {
    private PointProvider topLeftProvider;
    private PointProvider bottomRightProvider;
    private IntegerProvider brushSizeProvider;
    private DoubleProvider endPositionProvider;
    private ColorProvider colorProvider;
    private FileProvider brushFileProvider;

    private DrawLineService drawLineService;
    private BresenhemPixelProvider bresenhemPixelProvider;

    public DrawnEllipseHighlightProceduralEffect(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider) {
        super(visualMediaMetadata, interval);
        this.drawLineService = drawLineService;
        this.bresenhemPixelProvider = bresenhemPixelProvider;

    }

    public DrawnEllipseHighlightProceduralEffect(DrawnEllipseHighlightProceduralEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public DrawnEllipseHighlightProceduralEffect(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider2) {
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

        int brushSize = (int) (brushSizeProvider.getValueAt(relativePosition) * request.getScale());
        if (brushSize < 1) {
            brushSize = 1;
        }

        Point topLeft = topLeftProvider.getValueAt(relativePosition);
        topLeft = new Point(topLeft.x * request.getExpectedWidth(), topLeft.y * request.getExpectedHeight());

        Point bottomRight = bottomRightProvider.getValueAt(relativePosition);
        bottomRight = new Point(bottomRight.x * request.getExpectedWidth(), bottomRight.y * request.getExpectedHeight());

        int width = (int) Math.abs(bottomRight.x - topLeft.x);
        int height = (int) Math.abs(bottomRight.y - topLeft.y);

        int centerX = (int) (topLeft.x + width / 2);
        int centerY = (int) (bottomRight.y - height / 2);

        String brushFilePath = brushFileProvider.getValueOrDefault(relativePosition, "classpath:/brushes/Sponge-02.gbr");

        Color color = colorProvider.getValueAt(relativePosition);

        if (width > 0 && height > 0) {
            DrawLineRequest drawLineRequest = DrawLineRequest.builder()
                    .withBrushFilePath(brushFilePath)
                    .withBrushSize(brushSize)
                    .withColor(color)
                    .withPixels(bresenhemPixelProvider.ellipsePixels(centerX, centerY, width, height))
                    .withProgress(progress)
                    .withResult(result)
                    .build();

            drawLineService.drawLine(drawLineRequest);
        }

        return result;
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        topLeftProvider = new PointProvider(doubleProviderWithDefaultValue(0.3), doubleProviderWithDefaultValue(0.4));
        bottomRightProvider = new PointProvider(doubleProviderWithDefaultValue(0.7), doubleProviderWithDefaultValue(0.6));
        colorProvider = ColorProvider.fromDefaultValue(0, 0, 0);
        brushSizeProvider = new IntegerProvider(1, 200, new MultiKeyframeBasedDoubleInterpolator(70.0));
        endPositionProvider = new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(2.0));
        brushFileProvider = new FileProvider("gbr", new StepStringInterpolator());
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();
        LineProvider lineProvider = new LineProvider(topLeftProvider, bottomRightProvider);

        ValueProviderDescriptor areaProvider = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineProvider)
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

    private DoubleProvider doubleProviderWithDefaultValue(double defaultValue) {
        return new DoubleProvider(IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(defaultValue));
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new DrawnEllipseHighlightProceduralEffect(this, cloneRequestMetadata);
    }

}
