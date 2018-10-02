package com.helospark.tactview.core.timeline.proceduralclip;

import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.TimelineClip;

public interface ProceduralClipFactoryChainItem {

    public TimelineClip create(AddClipRequest request);

    public boolean doesSupport(AddClipRequest request);

    public String getProceduralClipName();

    public String getProceduralClipId();
}
