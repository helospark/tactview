package com.helospark.tactview.ui.javafx.clip.chain;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineClip;

public class ClipContextMenuChainItemRequest {
    private TimelineClip primaryClip;
    private List<TimelineClip> allClips;

    public ClipContextMenuChainItemRequest(TimelineClip clip, List<TimelineClip> allClips) {
        this.primaryClip = clip;
        this.allClips = allClips;
    }

    public TimelineClip getPrimaryClip() {
        return primaryClip;
    }

    public List<TimelineClip> getAllClips() {
        return allClips;
    }

}
