package com.helospark.tactview.core.timeline.proceduralclip.highlight;

import static com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.blendmode.impl.NormalBlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.FileProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.framemerge.AlphaBlitService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.BresenhemPixelProvider;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.brush.GetBrushRequest;
import com.helospark.tactview.core.util.brush.ScaledBrushProvider;

public class DrawnHighlightProceduralEffect extends ProceduralVisualClip {
    private PointProvider topLeftProvider;
    private PointProvider bottomRightProvider;
    private IntegerProvider brushSizeProvider;
    private DoubleProvider endPositionProvider;
    private ColorProvider colorProvider;
    private FileProvider brushFileProvider;

    private AlphaBlitService alphaBlitService;
    private NormalBlendModeStrategy normalBlendModeStrategy;
    private ScaledBrushProvider scaledBrushProvider;
    private BresenhemPixelProvider bresenhemPixelProvider;

    public DrawnHighlightProceduralEffect(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, ScaledBrushProvider scaledBrushProvider, NormalBlendModeStrategy normalBlendModeStrategy,
            AlphaBlitService alphaBlitService, BresenhemPixelProvider bresenhemPixelProvider) {
        super(visualMediaMetadata, interval);
        this.scaledBrushProvider = scaledBrushProvider;
        this.alphaBlitService = alphaBlitService;
        this.normalBlendModeStrategy = normalBlendModeStrategy;
        this.bresenhemPixelProvider = bresenhemPixelProvider;

    }

    public DrawnHighlightProceduralEffect(DrawnHighlightProceduralEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public DrawnHighlightProceduralEffect(ImageMetadata metadata, JsonNode node, ScaledBrushProvider scaledBrushProvider2, NormalBlendModeStrategy normalBlendModeStrategy2,
            AlphaBlitService alphaBlitService2, BresenhemPixelProvider bresenhemPixelProvider2) {
        super(metadata, node);
        this.scaledBrushProvider = scaledBrushProvider2;
        this.alphaBlitService = alphaBlitService2;
        this.normalBlendModeStrategy = normalBlendModeStrategy2;
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

        String brushFilePath;
        File brushFile = brushFileProvider.getValueAt(relativePosition);
        if (brushFile.exists()) {
            brushFilePath = brushFile.getAbsolutePath();
        } else {
            brushFilePath = "classpath:/brushes/Oils-03.gbr";
        }

        Optional<ClipImage> brushImage = getBrush(brushFilePath, brushSize);
        if (brushImage.isPresent() && width > 0 && height > 0) {
            List<Vector2D> pixels = bresenhemPixelProvider.ellipsePixels(centerX, centerY, width, height);
            int spacing = 1;
            for (int i = 0; i < pixels.size() * progress; i += spacing) {
                int x = (int) pixels.get(i).getX();
                int y = (int) pixels.get(i).getY();
                alphaBlitService.alphaBlitImageIntoResult(result, brushImage.get(), x, y, normalBlendModeStrategy, 1.0);
            }
        }

        return result;
    }

    private Optional<ClipImage> getBrush(String brushFilePath, int brushSize) {
        try {
            GetBrushRequest brushRequest = GetBrushRequest.builder()
                    .withFilename(brushFilePath)
                    .withWidth(brushSize)
                    .withHeight(brushSize)
                    .build();

            return Optional.of(scaledBrushProvider.getBrushImage(brushRequest));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        topLeftProvider = new PointProvider(doubleProviderWithDefaultValue(0.3), doubleProviderWithDefaultValue(0.4));
        bottomRightProvider = new PointProvider(doubleProviderWithDefaultValue(0.7), doubleProviderWithDefaultValue(0.6));
        colorProvider = createColorProvider(0, 0, 0);
        brushSizeProvider = new IntegerProvider(1, 200, new MultiKeyframeBasedDoubleInterpolator(70.0));
        endPositionProvider = new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(2.0));
        brushFileProvider = new FileProvider("gbr", new StringInterpolator());
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

    private ColorProvider createColorProvider(double r, double g, double b) {
        return new ColorProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(r)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(g)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(b)));
    }

    @Override
    public TimelineClip cloneClip() {
        return new DrawnHighlightProceduralEffect(this);
    }

}
