package com.helospark.tactview.core.timeline.proceduralclip;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.GradientProceduralEffect;
import com.helospark.tactview.core.timeline.proceduralclip.singlecolor.SingleColorProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.text.TextProceduralClip;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;
import com.helospark.tactview.core.util.IndependentPixelOperation;

@Configuration
public class CoreClipFactoryChainItemConfiguration {

    @Bean
    public StandardProceduralClipFactoryChainItem singleColorProceduralClip(IndependentPixelOperation independentPixelOperation) {
        return new StandardProceduralClipFactoryChainItem("singlecolor", "Single color",
                request -> {
                    TimelineLength defaultLength = TimelineLength.ofMillis(30000);
                    ImageMetadata metadata = ImageMetadata.builder()
                            .withWidth(1920)
                            .withHeight(1080)
                            .withLength(defaultLength)
                            .build();
                    return new SingleColorProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), independentPixelOperation);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem textProceduralClip(BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter) {
        return new StandardProceduralClipFactoryChainItem("text", "Text",
                request -> {
                    TimelineLength defaultLength = TimelineLength.ofMillis(30000);
                    ImageMetadata metadata = ImageMetadata.builder()
                            .withWidth(1920)
                            .withHeight(1080)
                            .withLength(defaultLength)
                            .build();
                    return new TextProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), bufferedImageToClipFrameResultConverter);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem gradientProceduralClip(IndependentPixelOperation independentPixelOperation) {
        return new StandardProceduralClipFactoryChainItem("gradient", "Gradient",
                request -> {
                    TimelineLength defaultLength = TimelineLength.ofMillis(30000);
                    ImageMetadata metadata = ImageMetadata.builder()
                            .withWidth(1920)
                            .withHeight(1080)
                            .withLength(defaultLength)
                            .build();
                    return new GradientProceduralEffect(metadata, new TimelineInterval(request.getPosition(), defaultLength), independentPixelOperation);
                });
    }
}
