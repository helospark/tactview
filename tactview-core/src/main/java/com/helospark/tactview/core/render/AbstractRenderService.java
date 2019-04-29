package com.helospark.tactview.core.render;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.message.progress.ProgressDoneMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressInitializeMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

public abstract class AbstractRenderService implements RenderService {
    protected TimelineManagerRenderService timelineManagerRenderService;
    protected MessagingService messagingService;
    protected ScaleService scaleService;
    protected ProjectRepository projectRepository;

    public AbstractRenderService(TimelineManagerRenderService timelineManagerRenderService, MessagingService messagingService, ScaleService scaleService, ProjectRepository projectRepository) {
        this.timelineManagerRenderService = timelineManagerRenderService;
        this.messagingService = messagingService;
        this.scaleService = scaleService;
        this.projectRepository = projectRepository;
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

    protected AudioVideoFragment queryFrameAt(RenderRequest renderRequest, TimelinePosition currentPosition, Optional<Integer> sampleRate, Optional<Integer> bytesPerSample) {
        double upscale = renderRequest.getUpscale().doubleValue();

        double scaleMultiplier = (double) renderRequest.getWidth() / projectRepository.getWidth();

        TimelineManagerFramesRequest frameRequest = TimelineManagerFramesRequest.builder()
                .withPosition(currentPosition)
                .withPreviewWidth((int) (renderRequest.getWidth() * upscale))
                .withPreviewHeight((int) (renderRequest.getHeight() * upscale))
                .withScale(scaleMultiplier * upscale)
                .withAudioBytesPerSample(sampleRate)
                .withAudioBytesPerSample(bytesPerSample)
                .build();

        AudioVideoFragment frame = timelineManagerRenderService.getFrame(frameRequest);

        if (renderRequest.getUpscale().compareTo(BigDecimal.ONE) > 0.0) {
            ScaleRequest scaleRequest = ScaleRequest.builder()
                    .withImage(frame.getVideoResult())
                    .withNewWidth(renderRequest.getWidth())
                    .withNewHeight(renderRequest.getHeight())
                    .build();
            ClipImage scaledImage = scaleService.createScaledImage(scaleRequest);
            frame = frame.butFreeAndReplaceVideoFrame(scaledImage);
        }

        return frame;
    }

    protected abstract void renderInternal(RenderRequest renderRequest);

}
