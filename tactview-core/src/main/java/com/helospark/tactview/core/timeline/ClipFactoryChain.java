package com.helospark.tactview.core.timeline;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.MediaMetadata;

@Component
public class ClipFactoryChain {
    private List<ClipFactory> clipFactoryChain;

    public ClipFactoryChain(List<ClipFactory> clipFactoryChain) {
        this.clipFactoryChain = clipFactoryChain;
    }

    public TimelineClip createClip(AddClipRequest request) {
        return findFactory(request)
                .createClip(request);
    }

    public MediaMetadata readMetadata(AddClipRequest request) {
        return findFactory(request)
                .readMetadata(request);
    }

    private ClipFactory findFactory(AddClipRequest request) {
        return clipFactoryChain.stream()
                .filter(factory -> factory.doesSupport(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No clip factory found for " + request));
    }

}
