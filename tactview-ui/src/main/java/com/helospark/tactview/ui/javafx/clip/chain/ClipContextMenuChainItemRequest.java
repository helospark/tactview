package com.helospark.tactview.ui.javafx.clip.chain;

import com.helospark.tactview.core.timeline.TimelineClip;

public class ClipContextMenuChainItemRequest {
    private TimelineClip clip;

    public ClipContextMenuChainItemRequest(TimelineClip clip) {
        this.clip = clip;
    }

    public TimelineClip getClip() {
        return clip;
    }

}
