package com.helospark.tactview.core.timeline.framemerge;

import com.helospark.tactview.core.timeline.image.ClipImage;

public interface FrameMergerWithMask {

    ClipImage mergeFramesWithMask(FrameMergerWithMaskRequest request);

}