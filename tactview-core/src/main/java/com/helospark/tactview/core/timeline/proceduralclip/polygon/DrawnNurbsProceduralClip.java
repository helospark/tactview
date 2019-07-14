package com.helospark.tactview.core.timeline.proceduralclip.polygon;

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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.FileProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PolygonProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.timeline.proceduralclip.lines.impl.DrawLineRequest;
import com.helospark.tactview.core.timeline.proceduralclip.lines.impl.DrawLineService;
import com.helospark.tactview.core.util.BresenhemPixelProviderInterface;

public class DrawnNurbsProceduralClip extends ProceduralVisualClip {
    private PolygonProvider polygonProvider;
    private IntegerProvider brushSizeProvider;
    private DoubleProvider drawPercentageProvider;
    private ColorProvider colorProvider;
    private FileProvider brushFileProvider;
    private BooleanProvider closeProvider;

    private DrawLineService drawLineService;
    private BresenhemPixelProviderInterface bresenhemPixelProvider;

    public DrawnNurbsProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, DrawLineService drawLineService, BresenhemPixelProviderInterface bresenhemPixelProvider) {
        super(visualMediaMetadata, interval);
        this.drawLineService = drawLineService;
        this.bresenhemPixelProvider = bresenhemPixelProvider;

    }

    public DrawnNurbsProceduralClip(DrawnNurbsProceduralClip cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public DrawnNurbsProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, DrawLineService drawLineService, BresenhemPixelProviderInterface bresenhemPixelProvider2) {
        super(metadata, node, loadMetadata);
        this.drawLineService = drawLineService;
        this.bresenhemPixelProvider = bresenhemPixelProvider2;
    }

    @Override
    public ClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
        double progress = drawPercentageProvider.getValueAt(relativePosition);

        int brushSize = (int) (brushSizeProvider.getValueAt(relativePosition) * request.getScale());
        if (brushSize < 1) {
            brushSize = 1;
        }

        Polygon polygon = polygonProvider.getValueAt(relativePosition).multiplyPoints(new Point(request.getExpectedWidth(), request.getExpectedHeight()));

        String brushFilePath = brushFileProvider.getValueOrDefault(relativePosition, "classpath:/brushes/Sponge-02.gbr");

        Color color = colorProvider.getValueAt(relativePosition);

        boolean close = closeProvider.getValueAt(relativePosition);

        DrawLineRequest drawLineRequest = DrawLineRequest.builder()
                .withBrushFilePath(brushFilePath)
                .withBrushSize(brushSize)
                .withColor(color)
                .withPixels(bresenhemPixelProvider.nurbsPixels(polygon, close))
                .withProgress(progress)
                .withResult(result)
                .build();

        drawLineService.drawLine(drawLineRequest);

        return result;
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        polygonProvider = new PolygonProvider(List.of(new Point(0.2, 0.1), new Point(0.3, 0.9), new Point(0.2, 0.4), new Point(0.8, 0.1)));
        colorProvider = ColorProvider.fromDefaultValue(0, 0, 0);
        brushSizeProvider = new IntegerProvider(1, 200, new MultiKeyframeBasedDoubleInterpolator(70.0));
        drawPercentageProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
        brushFileProvider = new FileProvider("gbr", new StepStringInterpolator());
        closeProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor areaProvider = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(polygonProvider)
                .withName("NURBS")
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
        ValueProviderDescriptor closeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(closeProvider)
                .withName("close")
                .build();

        result.add(areaProvider);
        result.add(colorProviderDescriptor);
        result.add(brushSizeProviderDescriptor);
        result.add(endPositionProviderDesctiptor);
        result.add(brushProviderDescriptor);
        result.add(closeProviderDescriptor);

        return result;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new DrawnNurbsProceduralClip(this, cloneRequestMetadata);
    }

}
