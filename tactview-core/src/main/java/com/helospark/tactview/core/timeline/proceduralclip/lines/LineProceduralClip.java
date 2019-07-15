package com.helospark.tactview.core.timeline.proceduralclip.lines;

import static com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.ReflectionUtil;
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
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.FileProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.timeline.proceduralclip.lines.impl.DrawLineRequest;
import com.helospark.tactview.core.timeline.proceduralclip.lines.impl.DrawLineService;
import com.helospark.tactview.core.util.BresenhemPixelProvider;

public class LineProceduralClip extends ProceduralVisualClip {
    private BresenhemPixelProvider bresenhemPixelProvider;
    private DrawLineService drawLineService;

    private DoubleProvider progressProvider;
    private ColorProvider colorProvider;
    private FileProvider brushFileProvider;
    private PointProvider startPointProvider;
    private PointProvider endPointProvider;
    private IntegerProvider brushSizeProvider;

    public LineProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider) {
        super(visualMediaMetadata, interval);
        this.bresenhemPixelProvider = bresenhemPixelProvider;
        this.drawLineService = drawLineService;
    }

    public LineProceduralClip(LineProceduralClip lineProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(lineProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(lineProceduralClip, this);
    }

    public LineProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider) {
        super(metadata, node, loadMetadata);
        this.drawLineService = drawLineService;
        this.bresenhemPixelProvider = bresenhemPixelProvider;
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
        double progress = progressProvider.getValueAt(relativePosition);

        int brushSize = (int) (brushSizeProvider.getValueAt(relativePosition) * request.getScale());
        if (brushSize < 1) {
            brushSize = 1;
        }

        Point startPoint = startPointProvider.getValueAt(relativePosition);
        startPoint = new Point(startPoint.x * request.getExpectedWidth(), startPoint.y * request.getExpectedHeight());

        Point endPoint = endPointProvider.getValueAt(relativePosition);
        endPoint = new Point(endPoint.x * request.getExpectedWidth(), endPoint.y * request.getExpectedHeight());

        String brushFilePath = brushFileProvider.getValueOrDefault(relativePosition, "classpath:/brushes/Sponge-02.gbr");

        Color color = colorProvider.getValueAt(relativePosition);

        DrawLineRequest drawLineRequest = DrawLineRequest.builder()
                .withBrushFilePath(brushFilePath)
                .withBrushSize(brushSize)
                .withColor(color)
                .withPixels(bresenhemPixelProvider.linePixels(startPoint, endPoint))
                .withProgress(progress)
                .withResult(result)
                .build();

        drawLineService.drawLine(drawLineRequest);

        return result;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new LineProceduralClip(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        startPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.1), doubleProviderWithDefaultValue(0.1));
        endPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.9), doubleProviderWithDefaultValue(0.9));
        colorProvider = ColorProvider.fromDefaultValue(0.1, 0.1, 0.1);
        brushSizeProvider = new IntegerProvider(1, 200, new MultiKeyframeBasedDoubleInterpolator(70.0));
        brushFileProvider = new FileProvider("gbr", new StepStringInterpolator());
        progressProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();
        LineProvider lineProvider = new LineProvider(startPointProvider, endPointProvider);

        ValueProviderDescriptor lineProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineProvider)
                .withName("Line")
                .build();
        ValueProviderDescriptor colorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorProvider)
                .withName("color")
                .build();
        ValueProviderDescriptor brushSizeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(brushSizeProvider)
                .withName("bursh size")
                .build();
        ValueProviderDescriptor brushProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(brushFileProvider)
                .withName("brush")
                .build();
        ValueProviderDescriptor progressDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(progressProvider)
                .withName("progress")
                .build();

        result.add(lineProviderDescriptor);
        result.add(progressDescriptor);
        result.add(colorProviderDescriptor);
        result.add(brushSizeProviderDescriptor);
        result.add(brushProviderDescriptor);

        return result;
    }

    private DoubleProvider doubleProviderWithDefaultValue(double defaultValue) {
        return new DoubleProvider(IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(defaultValue));
    }

}
