package com.helospark.tactview.ui.javafx.repository.copypaste;

import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;

public class ClipCopyPasteDomain {
    public TimelineClip clipboardContent;
    public TimelineChannel timelineChannel;

    public ClipCopyPasteDomain(TimelineClip clipboardContent, TimelineChannel timelineChannel) {
        this.clipboardContent = clipboardContent;
        this.timelineChannel = timelineChannel;
    }
}
