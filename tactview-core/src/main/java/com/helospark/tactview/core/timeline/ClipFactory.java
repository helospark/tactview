package com.helospark.tactview.core.timeline;

import java.io.File;

public interface ClipFactory {

    boolean doesSupport(File file);

    TimelineClip createClip(File file, TimelinePosition position);

}
