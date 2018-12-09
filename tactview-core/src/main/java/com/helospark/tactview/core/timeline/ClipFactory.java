package com.helospark.tactview.core.timeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.decoder.MediaMetadata;

public interface ClipFactory {

    boolean doesSupport(AddClipRequest request);

    MediaMetadata readMetadata(AddClipRequest request);

    TimelineClip createClip(AddClipRequest request);

    public String getId();

    TimelineClip restoreClip(JsonNode savedClip);

}
