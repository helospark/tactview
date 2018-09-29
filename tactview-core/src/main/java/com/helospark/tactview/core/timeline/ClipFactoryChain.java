package com.helospark.tactview.core.timeline;

import java.io.File;
import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;

@Component
public class ClipFactoryChain {
    private List<ClipFactory> clipFactoryChain;

    public ClipFactoryChain(List<ClipFactory> clipFactoryChain) {
        this.clipFactoryChain = clipFactoryChain;
    }

    public TimelineClip createClip(File file, TimelinePosition position) {
        return findFactory(file)
                .createClip(file, position);
    }

    public VisualMediaMetadata readMetadata(File file) {
        return findFactory(file)
                .readMetadata(file);
    }

    private ClipFactory findFactory(File file) {
        return clipFactoryChain.stream()
                .filter(a -> a.doesSupport(file))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No clip factory found for " + file));
    }

}
