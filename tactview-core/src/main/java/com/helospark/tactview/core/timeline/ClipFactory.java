package com.helospark.tactview.core.timeline;

import java.io.File;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;

public interface ClipFactory {

    boolean doesSupport(File file);

    VisualMediaMetadata readMetadata(File file);

    TimelineClip createClip(File file, TimelinePosition position);

}
