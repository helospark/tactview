package com.helospark.tactview.core.render;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.message.progress.ProgressDoneMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressInitializeMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

public abstract class AbstractRenderService implements RenderService {
    protected TimelineManager timelineManager;
    protected MessagingService messagingService;

    public AbstractRenderService(TimelineManager timelineManager, MessagingService messagingService) {
        this.timelineManager = timelineManager;
        this.messagingService = messagingService;
    }

    @Override
    public void render(RenderRequest renderRequest) {
        BigDecimal endSeconds = renderRequest.getEndPosition().getSeconds();
        BigDecimal startSeconds = renderRequest.getStartPosition().getSeconds();

        int numberOfFrames = endSeconds.subtract(startSeconds).divide(renderRequest.getStep(), 20, RoundingMode.HALF_UP).intValue();
        messagingService.sendAsyncMessage(new ProgressInitializeMessage(renderRequest.getRenderId(), numberOfFrames));

        renderInternal(renderRequest);

        messagingService.sendAsyncMessage(new ProgressDoneMessage(renderRequest.getRenderId()));
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

    protected abstract void renderInternal(RenderRequest renderRequest);

}
