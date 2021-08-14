package com.helospark.tactview.core.render;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import javax.annotation.Generated;

import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineLength;
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

    protected AudioVideoFragment queryFrameAt(RenderRequestFrameRequest request) {
        RenderRequest renderRequest = request.renderRequest;
        TimelinePosition currentPosition = request.currentPosition;
        Optional<Integer> sampleRate = request.sampleRate;
        Optional<Integer> bytesPerSample = request.bytesPerSample;
        Optional<Integer> numberOfChannels = request.numberOfChannels;
        boolean needsVideo = request.needsVideo;
        boolean needsSound = request.needsSound;

        double upscale = renderRequest.getUpscale().doubleValue();

        double scaleMultiplier = (double) renderRequest.getWidth() / projectRepository.getWidth();

        TimelineManagerFramesRequest frameRequest = TimelineManagerFramesRequest.builder()
                .withPosition(currentPosition)
                .withPreviewWidth((int) (request.expectedWidth * upscale))
                .withPreviewHeight((int) (request.expectedHeight * upscale))
                .withScale(scaleMultiplier * upscale)
                .withAudioSampleRate(sampleRate)
                .withAudioBytesPerSample(bytesPerSample)
                .withNeedVideo(needsVideo)
                .withNeedSound(needsSound)
                .withNumberOfChannels(numberOfChannels)
                .withAudioLength(request.audioLength)
                .build();

        AudioVideoFragment frame = timelineManagerRenderService.getFrame(frameRequest).getAudioVideoFragment();

        if (frame.getVideoResult() != null && (renderRequest.getUpscale().compareTo(BigDecimal.ONE) > 0.0 ||
                frame.getVideoResult().getWidth() != request.expectedWidth || // due to +1 for width/height on odd size
                frame.getVideoResult().getHeight() != request.expectedHeight)) {
            ScaleRequest scaleRequest = ScaleRequest.builder()
                    .withImage(frame.getVideoResult())
                    .withNewWidth(request.expectedWidth)
                    .withNewHeight(request.expectedHeight)
                    .build();
            ClipImage scaledImage = scaleService.createScaledImage(scaleRequest);
            frame = frame.butFreeAndReplaceVideoFrame(scaledImage);
        }

        return frame;
    }

    protected abstract void renderInternal(RenderRequest renderRequest);

    static class RenderRequestFrameRequest {
        RenderRequest renderRequest;
        TimelinePosition currentPosition;
        Optional<Integer> sampleRate;
        Optional<Integer> bytesPerSample;
        Optional<Integer> numberOfChannels;
        Optional<TimelineLength> audioLength;
        boolean needsVideo;
        boolean needsSound;
        int expectedWidth;
        int expectedHeight;

        @Generated("SparkTools")
        private RenderRequestFrameRequest(Builder builder) {
            this.renderRequest = builder.renderRequest;
            this.currentPosition = builder.currentPosition;
            this.sampleRate = builder.sampleRate;
            this.bytesPerSample = builder.bytesPerSample;
            this.numberOfChannels = builder.numberOfChannels;
            this.needsVideo = builder.needsVideo;
            this.needsSound = builder.needsSound;
            this.expectedWidth = builder.expectedWidth;
            this.expectedHeight = builder.expectedHeight;
            this.audioLength = builder.audioLength;
        }

        @Generated("SparkTools")
        public static Builder builder() {
            return new Builder();
        }

        @Generated("SparkTools")
        public static final class Builder {
            private RenderRequest renderRequest;
            private TimelinePosition currentPosition;
            private Optional<Integer> sampleRate = Optional.empty();
            private Optional<Integer> bytesPerSample = Optional.empty();
            private Optional<Integer> numberOfChannels = Optional.empty();
            private Optional<TimelineLength> audioLength = Optional.empty();
            private boolean needsVideo;
            private boolean needsSound;
            private int expectedWidth;
            private int expectedHeight;

            private Builder() {
            }

            public Builder withRenderRequest(RenderRequest renderRequest) {
                this.renderRequest = renderRequest;
                return this;
            }

            public Builder withCurrentPosition(TimelinePosition currentPosition) {
                this.currentPosition = currentPosition;
                return this;
            }

            public Builder withSampleRate(Optional<Integer> sampleRate) {
                this.sampleRate = sampleRate;
                return this;
            }

            public Builder withBytesPerSample(Optional<Integer> bytesPerSample) {
                this.bytesPerSample = bytesPerSample;
                return this;
            }

            public Builder withNumberOfChannels(Optional<Integer> numberOfChannels) {
                this.numberOfChannels = numberOfChannels;
                return this;
            }

            public Builder withNeedsVideo(boolean needsVideo) {
                this.needsVideo = needsVideo;
                return this;
            }

            public Builder withNeedsSound(boolean needsSound) {
                this.needsSound = needsSound;
                return this;
            }

            public Builder withExpectedWidth(int expectedWidth) {
                this.expectedWidth = expectedWidth;
                return this;
            }

            public Builder withExpectedHeight(int expectedHeight) {
                this.expectedHeight = expectedHeight;
                return this;
            }

            public Builder withAudioLength(TimelineLength audioLength) {
                this.audioLength = Optional.ofNullable(audioLength);
                return this;
            }

            public RenderRequestFrameRequest build() {
                return new RenderRequestFrameRequest(this);
            }
        }
    }

}
