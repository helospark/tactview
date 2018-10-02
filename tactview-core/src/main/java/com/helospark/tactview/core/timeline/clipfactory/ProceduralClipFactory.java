package com.helospark.tactview.core.timeline.clipfactory;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralClipFactoryChainItem;

@Component
public class ProceduralClipFactory implements ClipFactory {
    private List<ProceduralClipFactoryChainItem> factories;

    public ProceduralClipFactory(List<ProceduralClipFactoryChainItem> factories) {
        this.factories = factories;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return factories.stream()
                .filter(factory -> factory.doesSupport(request))
                .findFirst()
                .isPresent();
    }

    @Override
    public VisualMediaMetadata readMetadata(AddClipRequest request) {
        return ImageMetadata.builder()
                .withWidth(1920)
                .withHeight(1080)
                .withLength(TimelineLength.ofMillis(5000))
                .build();
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        return factories.stream()
                .filter(factory -> factory.doesSupport(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nothing can handle " + request))
                .create(request);
    }

}
