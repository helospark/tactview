package com.helospark.tactview.core.timeline.effect.transition;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.effect.StandardEffectFactory;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVBasedGaussianBlur;
import com.helospark.tactview.core.timeline.effect.transition.alphatransition.AlphaTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.blurtransition.BlurTransition;
import com.helospark.tactview.core.timeline.effect.transition.floatout.FloatOutTransitionEffect;
import com.helospark.tactview.core.util.IndependentPixelOperation;

@Configuration
public class StandardTransitionEffectConfiguration {

    @Bean
    public StandardEffectFactory alphaTransitionEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new AlphaTransitionEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(1000)), independentPixelOperation))
                .withName("Alpha transition")
                .withSupportedEffectId("alphatransition")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .build();
    }

    @Bean
    public StandardEffectFactory floatOutTransitionEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new FloatOutTransitionEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(1000)), independentPixelOperation))
                .withName("Float out")
                .withSupportedEffectId("floatout")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .build();
    }

    @Bean
    public StandardEffectFactory blurTransitionEffect(OpenCVBasedGaussianBlur blurImplementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new BlurTransition(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(1000)), blurImplementation))
                .withName("Blur transition")
                .withSupportedEffectId("blurtransition")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .build();
    }

}
