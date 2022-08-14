package com.helospark.tactview.core.timeline.proceduralclip;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.effect.TimelineProceduralClipType;
import com.helospark.tactview.core.timeline.valueproviderclip.PointProviderClip;
import com.helospark.tactview.core.timeline.valueproviderclip.RmsProviderClip;
import com.helospark.tactview.core.util.AudioRmsCalculator;

@Configuration
public class CoreValueProviderClipsConfiguration {
    TimelineLength defaultLength = TimelineLength.ofMillis(30000);
    ImageMetadata metadata = ImageMetadata.builder()
            .withWidth(1920)
            .withHeight(1080)
            .withLength(defaultLength)
            .build();

    @Bean
    public StandardProceduralClipFactoryChainItem pointProviderClip() {
        return new StandardProceduralClipFactoryChainItem("pointprovider", "Point provider",
                request -> {
                    return new PointProviderClip(metadata, new TimelineInterval(request.getPosition(), defaultLength));
                },
                (node, loadMetadata) -> {
                    return new PointProviderClip(metadata, node, loadMetadata);
                }, TimelineProceduralClipType.VALUE_PROVIDER);
    }

    @Bean
    public StandardProceduralClipFactoryChainItem rmsProviderClip(AudioRmsCalculator audioRmsCalculator) {
        return new StandardProceduralClipFactoryChainItem("rmsprovider", "RMS provider",
                request -> {
                    return new RmsProviderClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), audioRmsCalculator);
                },
                (node, loadMetadata) -> {
                    return new RmsProviderClip(metadata, node, loadMetadata, audioRmsCalculator);
                }, TimelineProceduralClipType.VALUE_PROVIDER);
    }

}
