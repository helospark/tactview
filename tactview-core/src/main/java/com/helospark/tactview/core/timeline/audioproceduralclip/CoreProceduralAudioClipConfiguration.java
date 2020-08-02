package com.helospark.tactview.core.timeline.audioproceduralclip;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.audioproceduralclip.impl.ProceduralSquareWaveAudioClip;
import com.helospark.tactview.core.timeline.proceduralclip.audio.StandardProceduralAudioFactoryChainItem;
import com.helospark.tactview.core.util.IndependentPixelOperation;

@Configuration
public class CoreProceduralAudioClipConfiguration {
    TimelineLength defaultLength = TimelineLength.ofMillis(30000);
    AudioMediaMetadata defaultMetadata = AudioMediaMetadata.builder()
            .withBytesPerSample(2)
            .withChannels(2)
            .withLength(TimelineLength.ofSeconds(30.0))
            .withSampleRate(44100)
            .withBitRate(1000)
            .build();

    @Bean
    public StandardProceduralAudioFactoryChainItem squareWaveFactory(IndependentPixelOperation independentPixelOperation) {
        return new StandardProceduralAudioFactoryChainItem("squarewave", "Squarewave",
                request -> {
                    return new ProceduralSquareWaveAudioClip(new TimelineInterval(request.getPosition(), defaultLength), defaultMetadata);
                },
                (node, loadMetadata) -> {
                    return new ProceduralSquareWaveAudioClip(defaultMetadata, node, loadMetadata);
                });
    }
}
