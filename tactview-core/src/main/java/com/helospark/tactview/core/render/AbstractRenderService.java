package com.helospark.tactview.core.render;

import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelinePosition;

public abstract class AbstractRenderService implements RenderService {
    protected TimelineManager timelineManager;

    public AbstractRenderService(TimelineManager timelineManager) {
        this.timelineManager = timelineManager;
    }

    protected AudioVideoFragment queryFrameAt(RenderRequest renderRequest, TimelinePosition currentPosition) {
        TimelineManagerFramesRequest frameRequest = TimelineManagerFramesRequest.builder()
                .withFrameBufferSize(1)
                .withPosition(currentPosition)
                .withPreviewWidth(renderRequest.getWidth())
                .withPreviewHeight(renderRequest.getHeight())
                .withScale(1.0)
                .build();

        return timelineManager.getSingleFrame(frameRequest);
    }

}
