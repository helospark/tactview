package com.helospark.tactview.core.timeline.effect.orthogonal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.MovementType;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.RenderTypeHint;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.core.timeline.effect.rotate.RotateService;
import com.helospark.tactview.core.timeline.effect.rotate.RotateServiceRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.render.FrameExtender;
import com.helospark.tactview.core.util.ReflectionUtil;

public class OrthogonalTransformationEffect extends StatelessVideoEffect {
    private LineProvider translateScaleProvider;

    private BooleanProvider fitToRectangleScaleAndTranslate;
    private PointProvider translatePointProvider;
    private DoubleProvider scaleXProvider;
    private DoubleProvider scaleYProvider;
    private PointProvider scaleCenterProvider;

    private PointProvider rotationCenterProvider;
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
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this, cloneRequestMetadata);
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
        InterpolationLine line = translateScaleProvider.getValueAt(request.getEffectPosition());
        double angle = rotateProvider.getValueAt(request.getEffectPosition());

        Point rotationCenterPoint = rotationCenterProvider.getValueAt(request.getEffectPosition());
        Point scaleCenterPoint = scaleCenterProvider.getValueAt(request.getEffectPosition());

        int newWidth;
        int newHeight;

        int baseTranslateX, baseTranslateY;

        if (fitToRectangleScaleAndTranslate.getValueAt(request.getEffectPosition())) {
            newWidth = (int) (Math.abs(line.end.x - line.start.x) * request.getCanvasWidth());
            newHeight = (int) (Math.abs(line.end.y - line.start.y) * request.getCanvasHeight());

            baseTranslateX = (int) (line.start.x * request.getCanvasWidth());
            baseTranslateY = (int) (line.start.y * request.getCanvasHeight());
        } else {
            newWidth = (int) (scaleXProvider.getValueAt(request.getEffectPosition()) * currentFrame.getWidth());
            newHeight = (int) (scaleYProvider.getValueAt(request.getEffectPosition()) * currentFrame.getHeight());

            baseTranslateX = (int) ((translatePointProvider.getValueAt(request.getEffectPosition()).x * request.getCanvasWidth()) - (newWidth - currentFrame.getWidth()) * scaleCenterPoint.x);
            baseTranslateY = (int) ((translatePointProvider.getValueAt(request.getEffectPosition()).y * request.getCanvasHeight() - (newHeight - currentFrame.getHeight()) * scaleCenterPoint.y));
        }

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

        double relativeCenterPointX = scaledImage.getWidth() * (rotationCenterPoint.x - 0.5);
        double relativeCenterPointY = scaledImage.getHeight() * (rotationCenterPoint.y - 0.5);

        int a = (int) (((relativeCenterPointX) * Math.sin(angleRad)) + ((relativeCenterPointY) * Math.cos(angleRad)));
        int b = (int) (((relativeCenterPointX) * Math.cos(angleRad)) - ((relativeCenterPointY) * Math.sin(angleRad)));

        int translateX = (int) (((baseTranslateX) - (rotatedImage.getWidth() - scaledImage.getWidth()) / 2) - (b) + relativeCenterPointX);
        int translateY = (int) (((baseTranslateY) - (rotatedImage.getHeight() - scaledImage.getHeight()) / 2) - (a) + relativeCenterPointY);

        ClipImage extendedFrame = frameExtender.expandAndTranslate(rotatedImage, request.getCanvasWidth(), request.getCanvasHeight(), translateX, translateY);

        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(scaledImage.getBuffer());
        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(rotatedImage.getBuffer());

        return extendedFrame;
    }

    @Override
    protected void initializeValueProviderInternal() {
        translateScaleProvider = new LineProvider(new PointProvider(createDoubleProvider(0.0), createDoubleProvider(0.0)), new PointProvider(createDoubleProvider(0.5), createDoubleProvider(0.5)));
        rotationCenterProvider = new PointProvider(createDoubleProvider(0.5), createDoubleProvider(0.5));
        scaleCenterProvider = new PointProvider(createDoubleProvider(0.5), createDoubleProvider(0.5));
        rotateProvider = new DoubleProvider(-10000, 10000, new MultiKeyframeBasedDoubleInterpolator(0.0));
        fitToRectangleScaleAndTranslate = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
        translatePointProvider = PointProvider.ofNormalizedImagePosition(0.0, 0.0);
        scaleXProvider = new DoubleProvider(-3.0, 3.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
        scaleYProvider = new DoubleProvider(-3.0, 3.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
    }

    private DoubleProvider createDoubleProvider(double defaultValue) {
        return new DoubleProvider(SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(defaultValue));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor useSeparateTranslateAndScale = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fitToRectangleScaleAndTranslate)
                .withName("Fit to rectangle")
                .build();
        ValueProviderDescriptor translateScaleProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(translateScaleProvider)
                .withName("position, scale")
                .withEnabledIf(p -> fitToRectangleScaleAndTranslate.getValueAt(p))
                .build();

        ValueProviderDescriptor translateProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(translatePointProvider)
                .withName("Translate")
                .withEnabledIf(p -> !fitToRectangleScaleAndTranslate.getValueAt(p))
                .withGroup("translate")
                .withRenderHints(Map.of(RenderTypeHint.TYPE, MovementType.RELATIVE))
                .build();
        ValueProviderDescriptor scaleXProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(scaleXProvider)
                .withName("Scale X")
                .withEnabledIf(p -> !fitToRectangleScaleAndTranslate.getValueAt(p))
                .withGroup("scale")
                .build();
        ValueProviderDescriptor scaleYProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(scaleYProvider)
                .withName("Scale Y")
                .withEnabledIf(p -> !fitToRectangleScaleAndTranslate.getValueAt(p))
                .withGroup("scale")
                .build();
        ValueProviderDescriptor scaleCenterDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(scaleCenterProvider)
                .withName("relative scale center")
                .withEnabledIf(p -> !fitToRectangleScaleAndTranslate.getValueAt(p))
                .withGroup("scale")
                .build();

        ValueProviderDescriptor rotateProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(rotateProvider)
                .withName("rotate angle")
                .withGroup("rotation")
                .build();
        ValueProviderDescriptor rotateCenterDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(rotationCenterProvider)
                .withName("relative rotate center")
                .withGroup("rotation")
                .build();

        return Arrays.asList(useSeparateTranslateAndScale, translateScaleProviderDescriptor, translateProviderDescriptor, scaleXProviderDescriptor, scaleYProviderDescriptor, scaleCenterDescriptor,
                rotateProviderDescriptor,
                rotateCenterDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new OrthogonalTransformationEffect(this, cloneRequestMetadata);
    }

}
