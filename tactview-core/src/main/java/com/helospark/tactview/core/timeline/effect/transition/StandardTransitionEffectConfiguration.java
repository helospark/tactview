package com.helospark.tactview.core.timeline.effect.transition;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.effect.StandardEffectFactory;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVBasedGaussianBlur;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskApplier;
import com.helospark.tactview.core.timeline.effect.layermask.impl.calculator.LayerMaskAlphaToAlpha;
import com.helospark.tactview.core.timeline.effect.layermask.impl.calculator.LayerMaskGrayscaleToAlpha;
import com.helospark.tactview.core.timeline.effect.transition.alphatransition.AlphaTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.blurtransition.BlurTransition;
import com.helospark.tactview.core.timeline.effect.transition.chromadissolve.LightDissolveTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.flash.WhiteFlashTransition;
import com.helospark.tactview.core.timeline.effect.transition.floatout.FloatOutTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.random.RandomLineTransition;
import com.helospark.tactview.core.timeline.effect.transition.random.RandomPointTransition;
import com.helospark.tactview.core.timeline.effect.transition.random.ShuffledNumberService;
import com.helospark.tactview.core.timeline.effect.transition.shape.CircleTransition;
import com.helospark.tactview.core.timeline.effect.transition.shape.DiamondTransition;
import com.helospark.tactview.core.timeline.effect.transition.shape.LinearGradientTransition;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.service.LinearGradientService;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.service.RadialGradientService;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.PolygonRenderService;
import com.helospark.tactview.core.util.IndependentPixelOperation;

@Configuration
public class StandardTransitionEffectConfiguration {

    @Bean
    public StandardEffectFactory alphaTransitionEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new AlphaTransitionEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(1000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new AlphaTransitionEffect(node, loadMetadata, independentPixelOperation))
                .withName("Alpha transition")
                .withSupportedEffectId("alphatransition")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .build();
    }

    @Bean
    public StandardEffectFactory floatOutTransitionEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new FloatOutTransitionEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(1000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new FloatOutTransitionEffect(node, loadMetadata, independentPixelOperation))
                .withName("Float out")
                .withSupportedEffectId("floatout")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .build();
    }

    @Bean
    public StandardEffectFactory blurTransitionEffect(OpenCVBasedGaussianBlur blurImplementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new BlurTransition(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(1000)), blurImplementation))
                .withRestoreFactory((node, loadMetadata) -> new BlurTransition(node, loadMetadata, blurImplementation))
                .withName("Blur transition")
                .withSupportedEffectId("blurtransition")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .build();
    }

    @Bean
    public StandardEffectFactory lightDossolveTransitionEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new LightDissolveTransitionEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(1000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new LightDissolveTransitionEffect(node, loadMetadata, independentPixelOperation))
                .withName("Light dissolve transition")
                .withSupportedEffectId("lightdissolvetransition")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .build();
    }

    @Bean
    public StandardEffectFactory whiteFlashTransitionEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new WhiteFlashTransition(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(1000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new WhiteFlashTransition(node, loadMetadata, independentPixelOperation))
                .withName("White flash")
                .withSupportedEffectId("whiteflashtransition")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .build();
    }

    @Bean
    public StandardEffectFactory circleTransitionEffect(RadialGradientService radialGradientService, LayerMaskApplier layerMaskApplier, LayerMaskGrayscaleToAlpha layerMaskGrayscaleToAlpha) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new CircleTransition(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(2000)), radialGradientService, layerMaskApplier, layerMaskGrayscaleToAlpha))
                .withRestoreFactory((node, loadMetadata) -> new CircleTransition(node, loadMetadata, radialGradientService, layerMaskApplier, layerMaskGrayscaleToAlpha))
                .withName("Circle transition")
                .withSupportedEffectId("circletransition")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .build();
    }

    @Bean
    public StandardEffectFactory diamondTransitionEffect(PolygonRenderService polygonRenderService, LayerMaskApplier layerMaskApplier, LayerMaskAlphaToAlpha layerMaskAlphaToAlpha) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new DiamondTransition(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(2000)), polygonRenderService, layerMaskApplier, layerMaskAlphaToAlpha))
                .withRestoreFactory((node, loadMetadata) -> new DiamondTransition(node, loadMetadata, polygonRenderService, layerMaskApplier, layerMaskAlphaToAlpha))
                .withName("Diamond transition")
                .withSupportedEffectId("diamondtransition")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .build();
    }

    @Bean
    public StandardEffectFactory randomLineTransitionEffect(ShuffledNumberService shuffledNumberService) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new RandomLineTransition(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(2000)), shuffledNumberService))
                .withRestoreFactory((node, loadMetadata) -> new RandomLineTransition(node, loadMetadata, shuffledNumberService))
                .withName("Random line transition")
                .withSupportedEffectId("randomlinetransition")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .build();
    }

    @Bean
    public StandardEffectFactory dissolveTransitionEffect(ShuffledNumberService shuffledNumberService) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new RandomPointTransition(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(2000)), shuffledNumberService))
                .withRestoreFactory((node, loadMetadata) -> new RandomPointTransition(node, loadMetadata, shuffledNumberService))
                .withName("Dissolve transition")
                .withSupportedEffectId("dissovlvetransition")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .build();
    }

    @Bean
    public StandardEffectFactory linearGradientTransitionEffect(LinearGradientService linearGradientService, LayerMaskApplier layerMaskApplier, LayerMaskGrayscaleToAlpha layerMaskGrayscaleToAlpha) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new LinearGradientTransition(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(2000)), linearGradientService, layerMaskApplier,
                                layerMaskGrayscaleToAlpha))
                .withRestoreFactory((node, loadMetadata) -> new LinearGradientTransition(node, loadMetadata, linearGradientService, layerMaskApplier, layerMaskGrayscaleToAlpha))
                .withName("Linear gradient transition")
                .withSupportedEffectId("lineargradienttransition")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .build();
    }
}
