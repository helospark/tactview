package com.helospark.tactview.core.timeline.proceduralclip.highlight;

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
import com.helospark.tactview.core.timeline.blendmode.impl.Multiply2BlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.FileProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.timeline.proceduralclip.lines.impl.DrawLineRequest;
import com.helospark.tactview.core.timeline.proceduralclip.lines.impl.DrawLineService;
import com.helospark.tactview.core.util.BresenhemPixelProviderInterface;

public class HighlightPenProceduralEffect extends ProceduralVisualClip {
    private LineProvider lineProvider;
    private IntegerProvider brushSizeProvider;
    private DoubleProvider endPositionProvider;
    private ColorProvider colorProvider;
    private FileProvider brushFileProvider;

    private DrawLineService drawLineService;
    private BresenhemPixelProviderInterface bresenhemPixelProvider;

    public HighlightPenProceduralEffect(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, DrawLineService drawLineService, BresenhemPixelProviderInterface bresenhemPixelProvider) {
        super(visualMediaMetadata, interval);
        this.drawLineService = drawLineService;
        this.bresenhemPixelProvider = bresenhemPixelProvider;

    }

    public HighlightPenProceduralEffect(HighlightPenProceduralEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public HighlightPenProceduralEffect(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, DrawLineService drawLineService, BresenhemPixelProviderInterface bresenhemPixelProvider) {
        super(metadata, node, loadMetadata);
        this.drawLineService = drawLineService;
        this.bresenhemPixelProvider = bresenhemPixelProvider;
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

        InterpolationLine line = lineProvider.getValueAt(relativePosition).multiply(request.getExpectedWidth(), request.getExpectedHeight());

        int brushSize = (int) (brushSizeProvider.getValueAt(relativePosition) * request.getScale());
        if (brushSize < 1) {
            brushSize = 1;
        }

        String brushFilePath = brushFileProvider.getValueOrDefault(relativePosition, "classpath:/brushes/Sponge-02.gbr");

        Color color = colorProvider.getValueAt(relativePosition);

        DrawLineRequest drawLineRequest = DrawLineRequest.builder()
                .withBrushFilePath(brushFilePath)
                .withBrushSize(brushSize)
                .withColor(color)
                .withPixels(bresenhemPixelProvider.linePixels(line.start, line.end))
                .withProgress(progress)
                .withResult(result)
                .build();

        drawLineService.drawLine(drawLineRequest);

        return result;
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        lineProvider = LineProvider.ofNormalizedScreenCoordinates(0.3, 0.4, 0.8, 0.4);
        colorProvider = ColorProvider.fromDefaultValue(1.0, 1.0, 0);
        brushSizeProvider = new IntegerProvider(10, 500, new MultiKeyframeBasedDoubleInterpolator(70.0));
        endPositionProvider = new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(2.0));
        brushFileProvider = new FileProvider("gbr", new StepStringInterpolator());

        blendModeProvider.keyframeAdded(TimelinePosition.ofZero(), Multiply2BlendModeStrategy.ID);
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor areaProvider = ValueProviderDescriptor.builder()
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
        return new HighlightPenProceduralEffect(this, cloneRequestMetadata);
    }

}
