package com.helospark.tactview.core.timeline.effect.orthogonal;

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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.core.timeline.effect.rotate.RotateService;
import com.helospark.tactview.core.timeline.effect.rotate.RotateService.RotateServiceRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.render.FrameExtender;
import com.helospark.tactview.core.util.ReflectionUtil;

public class OrthogonalTransformationEffect extends StatelessVideoEffect {
    private LineProvider lineProvider;
    private PointProvider centerProvider;
    private DoubleProvider rotateProvider;

    private ScaleService scaleService;
    private RotateService rotateService;
    private FrameExtender frameExtender;

    public OrthogonalTransformationEffect(TimelineInterval interval, ScaleService scaleService, RotateService rotateService, FrameExtender frameExtender) {
        super(interval);
        this.scaleService = scaleService;
        this.rotateService = rotateService;
        this.frameExtender = frameExtender;
    }

    public OrthogonalTransformationEffect(OrthogonalTransformationEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public OrthogonalTransformationEffect(JsonNode node, LoadMetadata loadMetadata, ScaleService scaleService, RotateService rotateService, FrameExtender frameExtender) {
        super(node, loadMetadata);
        this.scaleService = scaleService;
        this.rotateService = rotateService;
        this.frameExtender = frameExtender;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        InterpolationLine line = lineProvider.getValueAt(request.getEffectPosition());
        double angle = rotateProvider.getValueAt(request.getEffectPosition());

        int newWidth = (int) (Math.abs(line.end.x - line.start.x) * request.getCanvasWidth());
        int newHeight = (int) (Math.abs(line.end.y - line.start.y) * request.getCanvasHeight());

        Point centerPoint = centerProvider.getValueAt(request.getEffectPosition());

        ScaleRequest scaleRequest = ScaleRequest.builder()
                .withImage(currentFrame)
                .withNewWidth(newWidth)
                .withNewHeight(newHeight)
                .build();

        ClipImage scaledImage = scaleService.createScaledImage(scaleRequest);

        RotateServiceRequest serviceRequest = RotateServiceRequest.builder()
                .withAngle(angle)
                .withImage(scaledImage)
                .withCenterX(0.5)
                .withCenterY(0.5)
                .build();

        ClipImage rotatedImage = rotateService.rotate(serviceRequest);

        double angleRad = Math.toRadians(-angle);

        double relativeCenterPointX = scaledImage.getWidth() * (centerPoint.x - 0.5);
        double relativeCenterPointY = scaledImage.getHeight() * (centerPoint.y - 0.5);

        int a = (int) (((relativeCenterPointX) * Math.sin(angleRad)) + ((relativeCenterPointY) * Math.cos(angleRad)));
        int b = (int) (((relativeCenterPointX) * Math.cos(angleRad)) - ((relativeCenterPointY) * Math.sin(angleRad)));

        int translateX = (int) (((line.start.x * request.getCanvasWidth()) - (rotatedImage.getWidth() - scaledImage.getWidth()) / 2) - (b) + relativeCenterPointX);
        int translateY = (int) (((line.start.y * request.getCanvasHeight()) - (rotatedImage.getHeight() - scaledImage.getHeight()) / 2) - (a) + relativeCenterPointY);

        ClipImage extendedFrame = frameExtender.expandAndTranslate(rotatedImage, request.getCanvasWidth(), request.getCanvasHeight(), translateX, translateY);

        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(scaledImage.getBuffer());
        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(rotatedImage.getBuffer());

        return extendedFrame;
    }

    @Override
    public void initializeValueProvider() {
        lineProvider = new LineProvider(new PointProvider(createDoubleProvider(0.0), createDoubleProvider(0.0)), new PointProvider(createDoubleProvider(0.5), createDoubleProvider(0.5)));
        centerProvider = new PointProvider(createDoubleProvider(0.5), createDoubleProvider(0.5));
        rotateProvider = new DoubleProvider(-10000, 10000, new MultiKeyframeBasedDoubleInterpolator(0.0));
    }

    private DoubleProvider createDoubleProvider(double defaultValue) {
        return new DoubleProvider(SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(defaultValue));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {

        ValueProviderDescriptor lineProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineProvider)
                .withName("position scale")
                .build();
        ValueProviderDescriptor rotateProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(rotateProvider)
                .withName("rotate angle")
                .build();
        ValueProviderDescriptor rotateCenterDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(centerProvider)
                .withName("relative rotate center")
                .build();

        return Arrays.asList(lineProviderDescriptor, rotateProviderDescriptor, rotateCenterDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new OrthogonalTransformationEffect(this, cloneRequestMetadata);
    }

}
