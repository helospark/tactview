package com.helospark.tactview.core.timeline.effect.scale;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.render.FrameExtender;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class ZoomEffect extends StatelessVideoEffect {

    private LineProvider toAreaProvider;
    private LineProvider fromAreaProvider;

    private BooleanProvider keepRatioProvider;

    private IndependentPixelOperation independentPixelOperation;
    private ScaleService scaleService;
    private FrameExtender frameExtender;

    public ZoomEffect(TimelineInterval interval, ScaleService scaleService, FrameExtender frameExtender, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.scaleService = scaleService;
        this.frameExtender = frameExtender;
        this.independentPixelOperation = independentPixelOperation;
    }

    public ZoomEffect(ZoomEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this, cloneRequestMetadata);
    }

    public ZoomEffect(JsonNode node, LoadMetadata loadMetadata, ScaleService scaleService2, FrameExtender frameExtender, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.scaleService = scaleService2;
        this.frameExtender = frameExtender;
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();

        InterpolationLine toArea = toAreaProvider.getValueAt(request.getEffectPosition()).multiply(request.getCanvasWidth(), request.getCanvasHeight());
        InterpolationLine fromArea = fromAreaProvider.getValueAt(request.getEffectPosition()).multiply(request.getCanvasWidth(), request.getCanvasHeight());
        boolean keepRatio = keepRatioProvider.getValueAt(request.getEffectPosition());

        int fromWidth = (int) Math.abs(fromArea.end.x - fromArea.start.x);
        int fromHeight = (int) Math.abs(fromArea.end.y - fromArea.start.y);

        int toWidth = (int) Math.abs(toArea.end.x - toArea.start.x);
        int toHeight = (int) Math.abs(toArea.end.y - toArea.start.y);

        toWidth = (toWidth <= 0 ? 1 : toWidth);
        toHeight = (toHeight <= 0 ? 1 : toHeight);
        fromWidth = (fromWidth <= 0 ? 1 : fromWidth);
        fromHeight = (fromHeight <= 0 ? 1 : fromHeight);

        if (keepRatio) {
            double ratio = (double) fromWidth / fromHeight;
            if (toWidth > toHeight) {
                toHeight = (int) (toWidth / ratio);
                toHeight = (toHeight <= 0 ? 1 : toHeight);
            } else {
                toWidth = (int) (toHeight * ratio);
                toWidth = (toWidth <= 0 ? 1 : toWidth);
            }
        }

        ClipImage imageToScale = ClipImage.fromSize(fromWidth, fromHeight);

        independentPixelOperation.executePixelTransformation(fromWidth, fromHeight, (x, y) -> {
            int fx = (int) (x + fromArea.start.x);
            int fy = (int) (y + fromArea.start.y);

            if (currentFrame.inBounds(fx, fy)) {
                imageToScale.copyColorFrom(currentFrame, fx, fy, x, y);
            }
        });

        ScaleRequest scaleRequest = ScaleRequest.builder()
                .withImage(imageToScale)
                .withNewWidth(toWidth)
                .withNewHeight(toHeight)
                .build();

        ClipImage scaledImage = scaleService.createScaledImage(scaleRequest);

        ClipImage extendedFrame = frameExtender.expandAndTranslate(scaledImage, request.getCanvasWidth(), request.getCanvasHeight(), (int) toArea.start.x, (int) toArea.start.y);

        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(scaledImage.getBuffer());
        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(imageToScale.getBuffer());

        return extendedFrame;
    }

    @Override
    protected void initializeValueProviderInternal() {
        toAreaProvider = LineProvider.ofNormalizedScreenCoordinates(0.0, 0.0, 1.0, 1.0);
        fromAreaProvider = LineProvider.ofNormalizedScreenCoordinates(0.3, 0.3, 0.8, 0.8);
        keepRatioProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor toAreaProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(toAreaProvider)
                .withName("To area")
                .build();
        ValueProviderDescriptor fromAreaProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fromAreaProvider)
                .withName("From area")
                .build();
        ValueProviderDescriptor keepRatioProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(keepRatioProvider)
                .withName("Keep ratio")
                .build();

        return Arrays.asList(toAreaProviderDescriptor, fromAreaProviderDescriptor, keepRatioProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new ZoomEffect(this, cloneRequestMetadata);
    }

}
