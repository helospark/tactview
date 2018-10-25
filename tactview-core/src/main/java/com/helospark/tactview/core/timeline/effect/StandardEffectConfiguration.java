package com.helospark.tactview.core.timeline.effect;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.effect.blur.BlurEffect;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVBasedGaussianBlur;
import com.helospark.tactview.core.timeline.effect.contractbrightness.BrightnessContrassEffect;
import com.helospark.tactview.core.timeline.effect.desaturize.DesaturizeEffect;
import com.helospark.tactview.core.timeline.effect.gamma.GammaEffect;
import com.helospark.tactview.core.timeline.effect.rotate.OpenCVRotateEffectImplementation;
import com.helospark.tactview.core.timeline.effect.rotate.RotateEffect;
import com.helospark.tactview.core.timeline.effect.scale.OpenCVScaleEffectImplementation;
import com.helospark.tactview.core.timeline.effect.scale.ScaleEffect;
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
}
