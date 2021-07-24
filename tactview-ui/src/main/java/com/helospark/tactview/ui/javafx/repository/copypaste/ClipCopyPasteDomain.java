package com.helospark.tactview.ui.javafx.repository.copypaste;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class ClipCopyPasteDomain {
    public List<CopiedClipData> copiedData;
    public TimelinePosition relativeEndPosition;

    public ClipCopyPasteDomain(List<CopiedClipData> copiedData, TimelinePosition relativeEndPosition) {
        this.copiedData = copiedData;
        this.relativeEndPosition = relativeEndPosition;
    }

    public static class CopiedClipData {
        public TimelineClip clipboardContent;
        public TimelineChannel timelineChannel;
        public TimelinePosition relativeOffset;

        public CopiedClipData(TimelineClip clipboardContent, TimelineChannel timelineChannel, TimelinePosition relativeOffset) {
            this.clipboardContent = clipboardContent;
            this.timelineChannel = timelineChannel;
            this.relativeOffset = relativeOffset;
        }

    }
}
