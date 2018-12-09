package com.helospark.tactview.core.timeline.audioeffect;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.audioeffect.volume.VolumeAudioEffect;
import com.helospark.tactview.core.timeline.effect.StandardEffectFactory;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVBasedGaussianBlur;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Configuration
public class AudioStandardEffectConfiguration {

    @Bean
    public StandardEffectFactory volumeEffect(OpenCVBasedGaussianBlur gaussianBlur, MessagingService messagingService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new VolumeAudioEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(10000))))
                .withRestoreFactory(node -> new VolumeAudioEffect(node))
                .withName("Volume")
                .withSupportedEffectId("volume")
                .withSupportedClipTypes(List.of(TimelineClipType.AUDIO))
                .build();
    }
}
