package com.helospark.tactview.core.render;

import java.nio.ByteBuffer;

import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelinePosition;

public abstract class AbstractRenderService implements RenderService {
    protected TimelineManager timelineManager;

    public AbstractRenderService(TimelineManager timelineManager) {
        this.timelineManager = timelineManager;
    }

    protected ByteBuffer queryFrameAt(RenderRequest renderRequest, TimelinePosition currentPosition) {
        TimelineManagerFramesRequest frameRequest = TimelineManagerFramesRequest.builder()
                .withFrameBufferSize(1)
                .withPosition(currentPosition)
                .withPreviewWidth(renderRequest.getWidth())
                .withPreviewHeight(renderRequest.getHeight())
                .withScale(1.0)
                .build();

        ByteBuffer frame = timelineManager.getSingleFrame(frameRequest);
        return frame;
    }

}
