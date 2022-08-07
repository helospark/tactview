package com.helospark.tactview.core.timeline.proceduralclip.polygon;

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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.FileProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PolygonProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.timeline.proceduralclip.lines.impl.DrawLineRequest;
import com.helospark.tactview.core.timeline.proceduralclip.lines.impl.DrawLineService;
import com.helospark.tactview.core.util.BresenhemPixelProvider;
import com.helospark.tactview.core.util.ReflectionUtil;

public class DrawnPolygonProceduralClip extends ProceduralVisualClip {
    private PolygonProvider polygonProvider;
    private IntegerProvider brushSizeProvider;
    private DoubleProvider drawPercentageProvider;
    private ColorProvider colorProvider;
    private FileProvider brushFileProvider;

    private DrawLineService drawLineService;
    private BresenhemPixelProvider bresenhemPixelProvider;

    public DrawnPolygonProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider) {
        super(visualMediaMetadata, interval);
        this.drawLineService = drawLineService;
        this.bresenhemPixelProvider = bresenhemPixelProvider;

    }

    public DrawnPolygonProceduralClip(DrawnPolygonProceduralClip cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this, cloneRequestMetadata);
    }

    public DrawnPolygonProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider2) {
        super(metadata, node, loadMetadata);
        this.drawLineService = drawLineService;
        this.bresenhemPixelProvider = bresenhemPixelProvider2;
    }

    @Override
    public ClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
        double progress = drawPercentageProvider.getValueAt(relativePosition, request.getEvaluationContext());

        int brushSize = (int) (brushSizeProvider.getValueAt(relativePosition, request.getEvaluationContext()) * request.getScale());
        if (brushSize < 1) {
            brushSize = 1;
        }

        Polygon polygon = polygonProvider.getValueAt(relativePosition, request.getEvaluationContext()).multiplyPoints(new Point(request.getExpectedWidth(), request.getExpectedHeight()));

        String brushFilePath = brushFileProvider.getValueOrDefault(relativePosition, "classpath:/brushes/Sponge-02.gbr", request.getEvaluationContext());

        Color color = colorProvider.getValueAt(relativePosition, request.getEvaluationContext());

        DrawLineRequest drawLineRequest = DrawLineRequest.builder()
                .withBrushFilePath(brushFilePath)
                .withBrushSize(brushSize)
                .withColor(color)
                .withPixels(bresenhemPixelProvider.polygonPixels(polygon))
                .withProgress(progress)
                .withResult(result)
                .build();

        drawLineService.drawLine(drawLineRequest);

        return result;
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        polygonProvider = new PolygonProvider(List.of(new Point(0.5, 0.1), new Point(0.8, 0.8), new Point(0.2, 0.8)));
        colorProvider = ColorProvider.fromDefaultValue(0, 0, 0);
        brushSizeProvider = new IntegerProvider(1, 200, new MultiKeyframeBasedDoubleInterpolator(70.0));
        drawPercentageProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
        brushFileProvider = new FileProvider("*.gbr", new StepStringInterpolator());
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor areaProvider = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(polygonProvider)
                .withName("Polygon")
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
                .withKeyframeableEffect(drawPercentageProvider)
                .withName("draw percent")
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
        return new DrawnPolygonProceduralClip(this, cloneRequestMetadata);
    }

}
