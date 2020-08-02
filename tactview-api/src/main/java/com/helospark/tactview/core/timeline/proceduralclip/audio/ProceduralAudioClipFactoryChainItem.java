package com.helospark.tactview.core.timeline.proceduralclip.audio;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;

public interface ProceduralAudioClipFactoryChainItem {
    public ProceduralAudioClip create(AddClipRequest request);

    public ProceduralAudioClip restoreClip(JsonNode node, LoadMetadata loadMetadata);

    public boolean doesSupport(AddClipRequest request);

    public String getProceduralClipName();

    public String getProceduralClipId();

    public String getId();
}
