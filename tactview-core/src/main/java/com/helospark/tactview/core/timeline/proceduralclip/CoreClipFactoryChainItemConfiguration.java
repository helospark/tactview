package com.helospark.tactview.core.timeline.proceduralclip;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;

@Configuration
public class CoreClipFactoryChainItemConfiguration {

    @Bean
    public StandardProceduralClipFactoryChainItem singleColorProceduralClip() {
        return new StandardProceduralClipFactoryChainItem("singlecolor", "Single color",
                request -> {
                    TimelineLength defaultLength = TimelineLength.ofMillis(5000);
                    ImageMetadata metadata = ImageMetadata.builder()
                            .withWidth(1920)
                            .withHeight(1080)
                            .withLength(defaultLength)
                            .build();
                    return new SingleColorProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength));
                });
    }
}
