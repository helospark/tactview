package com.helospark.tactview.core.timeline;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;

public interface ClipFactory {

    boolean doesSupport(AddClipRequest request);

    VisualMediaMetadata readMetadata(AddClipRequest request);

    TimelineClip createClip(AddClipRequest request);

}
