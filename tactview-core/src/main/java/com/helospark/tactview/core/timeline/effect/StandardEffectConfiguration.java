package com.helospark.tactview.core.timeline.effect;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.effect.blur.BlurEffect;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVBasedGaussianBlur;
import com.helospark.tactview.core.timeline.effect.cartoon.CartoonEffect;
import com.helospark.tactview.core.timeline.effect.cartoon.opencv.OpenCVCartoonEffectImplementation;
import com.helospark.tactview.core.timeline.effect.colorize.ColorizeEffect;
import com.helospark.tactview.core.timeline.effect.contractbrightness.BrightnessContrassEffect;
import com.helospark.tactview.core.timeline.effect.denoise.DenoiseEffect;
import com.helospark.tactview.core.timeline.effect.denoise.opencv.OpenCVBasedDenoiseEffect;
import com.helospark.tactview.core.timeline.effect.desaturize.DesaturizeEffect;
import com.helospark.tactview.core.timeline.effect.edgedetect.EdgeDetectEffect;
import com.helospark.tactview.core.timeline.effect.edgedetect.opencv.OpenCVEdgeDetectImplementation;
import com.helospark.tactview.core.timeline.effect.erodedilate.ErodeDilateEffect;
import com.helospark.tactview.core.timeline.effect.erodedilate.opencv.OpenCVErodeDilateImplementation;
import com.helospark.tactview.core.timeline.effect.gamma.GammaEffect;
import com.helospark.tactview.core.timeline.effect.glow.LightGlowEffect;
import com.helospark.tactview.core.timeline.effect.greenscreen.GreenScreenEffect;
import com.helospark.tactview.core.timeline.effect.greenscreen.opencv.OpenCVGreenScreenImplementation;
import com.helospark.tactview.core.timeline.effect.histogramequization.HistogramEquizationEffect;
import com.helospark.tactview.core.timeline.effect.histogramequization.opencv.OpenCVHistogramEquizerImplementation;
import com.helospark.tactview.core.timeline.effect.invert.InvertEffect;
import com.helospark.tactview.core.timeline.effect.mirror.MirrorEffect;
import com.helospark.tactview.core.timeline.effect.pencil.PencilSketchEffect;
import com.helospark.tactview.core.timeline.effect.pencil.opencv.OpenCVPencilSketchImplementation;
import com.helospark.tactview.core.timeline.effect.pixelize.PixelizeEffect;
import com.helospark.tactview.core.timeline.effect.rotate.OpenCVRotateEffectImplementation;
import com.helospark.tactview.core.timeline.effect.rotate.RotateEffect;
import com.helospark.tactview.core.timeline.effect.scale.OpenCVScaleEffectImplementation;
import com.helospark.tactview.core.timeline.effect.scale.ScaleEffect;
import com.helospark.tactview.core.timeline.effect.television.TelevisionRgbLinesEffect;
import com.helospark.tactview.core.timeline.effect.threshold.AdaptiveThresholdEffect;
import com.helospark.tactview.core.timeline.effect.threshold.SimpleThresholdEffect;
import com.helospark.tactview.core.timeline.effect.threshold.opencv.OpenCVThresholdImplementation;
import com.helospark.tactview.core.timeline.effect.warp.TrigonometricWrapEffect;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Configuration
public class StandardEffectConfiguration {

    @Bean
    public StandardEffectFactory blurEffect(OpenCVBasedGaussianBlur gaussianBlur, MessagingService messagingService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new BlurEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(10000)), gaussianBlur))
                .withMessagingService(messagingService)
                .withName("Gaussian blur")
                .withSupportedEffectId("gaussianblur")
                .build();
    }

    @Bean
    public StandardEffectFactory desaturizeEffect(OpenCVBasedGaussianBlur gaussianBlur, MessagingService messagingService, IndependentPixelOperation independentPixelOperations) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new DesaturizeEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperations))
                .withMessagingService(messagingService)
                .withName("Desaturize")
                .withSupportedEffectId("desaturize")
                .build();
    }

    @Bean
    public StandardEffectFactory scaleEffect(OpenCVScaleEffectImplementation implementation, MessagingService messagingService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new ScaleEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), implementation))
                .withMessagingService(messagingService)
                .withName("Scale")
                .withSupportedEffectId("scale")
                .build();
    }

    @Bean
    public StandardEffectFactory rotateEffect(OpenCVRotateEffectImplementation implementation, MessagingService messagingService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new RotateEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), implementation))
                .withMessagingService(messagingService)
                .withName("Rotate")
                .withSupportedEffectId("rotate")
                .build();
    }

    @Bean
    public StandardEffectFactory brightnessContrastEffect(MessagingService messagingService, IndependentPixelOperation independentPixelOperations) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new BrightnessContrassEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperations))
                .withMessagingService(messagingService)
                .withName("Brightness")
                .withSupportedEffectId("brightesscontrast")
                .build();
    }

    @Bean
    public StandardEffectFactory gammaEffect(MessagingService messagingService, IndependentPixelOperation independentPixelOperations) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new GammaEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperations))
                .withMessagingService(messagingService)
                .withName("Gamma")
                .withSupportedEffectId("gamma")
                .build();
    }

    @Bean
    public StandardEffectFactory invertEffect(MessagingService messagingService, IndependentPixelOperation independentPixelOperations) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new InvertEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperations))
                .withMessagingService(messagingService)
                .withName("Invert")
                .withSupportedEffectId("invert")
                .build();
    }

    @Bean
    public StandardEffectFactory denoiseEffect(MessagingService messagingService, OpenCVBasedDenoiseEffect openCVBasedDenoiseEffect) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new DenoiseEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), openCVBasedDenoiseEffect))
                .withMessagingService(messagingService)
                .withName("Denoise")
                .withSupportedEffectId("denoise")
                .build();
    }

    @Bean
    public StandardEffectFactory adaptiveThresholdEffect(MessagingService messagingService, OpenCVThresholdImplementation openCVThresholdImplementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new AdaptiveThresholdEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), openCVThresholdImplementation))
                .withMessagingService(messagingService)
                .withName("Adaptive threshold")
                .withSupportedEffectId("adaptivethreshold")
                .build();
    }

    @Bean
    public StandardEffectFactory thresholdEffect(MessagingService messagingService, IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new SimpleThresholdEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withMessagingService(messagingService)
                .withName("Threshold")
                .withSupportedEffectId("threshold")
                .build();
    }

    @Bean
    public StandardEffectFactory mirrorEffect(MessagingService messagingService, IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new MirrorEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withMessagingService(messagingService)
                .withName("Mirror")
                .withSupportedEffectId("mirror")
                .build();
    }

    @Bean
    public StandardEffectFactory colorize(MessagingService messagingService, IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new ColorizeEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withMessagingService(messagingService)
                .withName("Colorize")
                .withSupportedEffectId("colorize")
                .build();
    }

    @Bean
    public StandardEffectFactory pixelize(MessagingService messagingService, IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new PixelizeEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withMessagingService(messagingService)
                .withName("Pixelize")
                .withSupportedEffectId("pixelize")
                .build();
    }

    @Bean
    public StandardEffectFactory erodeDilate(MessagingService messagingService, OpenCVErodeDilateImplementation openCVErodeDilateImplementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new ErodeDilateEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), openCVErodeDilateImplementation))
                .withMessagingService(messagingService)
                .withName("Erode/Dilate")
                .withSupportedEffectId("erodedilate")
                .build();
    }

    @Bean
    public StandardEffectFactory edgeDetect(MessagingService messagingService, OpenCVEdgeDetectImplementation openCVEdgeDetectImplementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new EdgeDetectEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), openCVEdgeDetectImplementation))
                .withMessagingService(messagingService)
                .withName("Edge detect")
                .withSupportedEffectId("edgedetect")
                .build();
    }

    @Bean
    public StandardEffectFactory greenScreen(MessagingService messagingService, OpenCVGreenScreenImplementation openCVGreenScreenImplementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new GreenScreenEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), openCVGreenScreenImplementation))
                .withMessagingService(messagingService)
                .withName("Green screen")
                .withSupportedEffectId("greenscreen")
                .build();
    }

    @Bean
    public StandardEffectFactory equizeHistogram(MessagingService messagingService, OpenCVHistogramEquizerImplementation implementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new HistogramEquizationEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), implementation))
                .withMessagingService(messagingService)
                .withName("Equize histogram")
                .withSupportedEffectId("equize histogram")
                .build();
    }

    @Bean
    public StandardEffectFactory cartoonEffect(MessagingService messagingService, OpenCVCartoonEffectImplementation implementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new CartoonEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), implementation))
                .withMessagingService(messagingService)
                .withName("Cartoon")
                .withSupportedEffectId("cartoon")
                .build();
    }

    @Bean
    public StandardEffectFactory pencilSketch(MessagingService messagingService, OpenCVPencilSketchImplementation implementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new PencilSketchEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), implementation))
                .withMessagingService(messagingService)
                .withName("Pencil")
                .withSupportedEffectId("pencil")
                .build();
    }

    @Bean
    public StandardEffectFactory warpEffect(MessagingService messagingService, IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new TrigonometricWrapEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withMessagingService(messagingService)
                .withName("Warp")
                .withSupportedEffectId("warp")
                .build();
    }

    @Bean
    public StandardEffectFactory televisionRgbLinesEffect(MessagingService messagingService, IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new TelevisionRgbLinesEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withMessagingService(messagingService)
                .withName("Television RGB")
                .withSupportedEffectId("televisionrgb")
                .build();
    }

    @Bean
    public StandardEffectFactory lightGlowEffect(MessagingService messagingService, IndependentPixelOperation independentPixelOperation, OpenCVBasedGaussianBlur blueImplementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new LightGlowEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), blueImplementation, independentPixelOperation))
                .withMessagingService(messagingService)
                .withName("Light glow")
                .withSupportedEffectId("lightglow")
                .build();
    }
}
