package com.helospark.tactview.core.timeline;

import java.io.File;

public interface ClipFactory {

    public TimelineClip createClip(File file);

    public boolean doesSupport(File file);

}
