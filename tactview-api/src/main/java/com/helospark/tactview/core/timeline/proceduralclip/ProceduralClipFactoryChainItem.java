package com.helospark.tactview.core.timeline.proceduralclip;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.effect.TimelineProceduralClipType;

public interface ProceduralClipFactoryChainItem {

    public ProceduralVisualClip create(AddClipRequest request);

    public ProceduralVisualClip restoreClip(JsonNode node, LoadMetadata loadMetadata);

    public boolean doesSupport(AddClipRequest request);

    public String getProceduralClipName();

    public String getProceduralClipId();

    public String getId();

    public TimelineProceduralClipType getType();
}
