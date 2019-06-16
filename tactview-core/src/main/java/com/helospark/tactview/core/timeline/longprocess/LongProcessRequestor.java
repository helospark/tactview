package com.helospark.tactview.core.timeline.longprocess;

import static com.helospark.tactview.core.timeline.message.progress.ProgressType.LONG_PROCESS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.message.progress.ProgressAdvancedMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressDoneMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressInitializeMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class LongProcessRequestor {
    ExecutorService executor = Executors.newSingleThreadExecutor();

    private ProjectRepository projectRepository;
    private MessagingService messagingService;
    private TimelineManagerAccessor timelineManagerAccessor;

    public LongProcessRequestor(ProjectRepository projectRepository, MessagingService messagingService) {
        this.projectRepository = projectRepository;
        this.messagingService = messagingService;
    }

    public <T extends StatelessVideoEffect & LongProcessVisualImagePushAware> void requestFrames(T target, LongProcessFrameRequest longProcessFrameRequest) {
        VisualTimelineClip clip = (VisualTimelineClip) timelineManagerAccessor.findClipForEffect(target.getId()).get();
        Optional<Integer> effectChannel = clip.getEffectChannelIndex(target.getId());
        executor.submit(() -> {
            String jobId = UUID.randomUUID().toString();
            try {
                TimelineInterval interval = target.getGlobalInterval();
                BigDecimal step = BigDecimal.ONE.divide(projectRepository.getFps(), 10, RoundingMode.HALF_UP);
                messagingService.sendAsyncMessage(new ProgressInitializeMessage(jobId, interval.getLength().getSeconds().divide(step, 10, RoundingMode.HALF_UP).intValue(), LONG_PROCESS));

                TimelinePosition currentPosition = interval.getStartPosition();

                target.beginToPushLongImages();

                while (currentPosition.isLessThan(interval.getEndPosition())) {
                    GetFrameRequest frameRequest = GetFrameRequest.builder()
                            .withScale(1.0)
                            .withPosition(currentPosition)
                            .withExpectedWidth(projectRepository.getWidth())
                            .withExpectedHeight(projectRepository.getHeight())
                            .withApplyEffects(true)
                            .withApplyEffectsLessThanEffectChannel(effectChannel)
                            .build();

                    ReadOnlyClipImage frameResult = clip.getFrame(frameRequest);

                    LongProcessImagePushRequest pushRequest = LongProcessImagePushRequest.builder()
                            .withImage(frameResult)
                            .withPosition(currentPosition)
                            .build();

                    target.longProcessImage(pushRequest);

                    messagingService.sendAsyncMessage(new ProgressAdvancedMessage(jobId, 1));

                    currentPosition = currentPosition.add(step);
                    GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frameResult.getBuffer());
                }
            } finally {
                target.endToPushLongImages();
                messagingService.sendAsyncMessage(new ProgressDoneMessage(jobId));
            }
        });
    }

    // Due to circular dependencies. TODO: avoid
    public void setTimelineManagerAccessor(TimelineManagerAccessor timelineManagerAccessor) {
        this.timelineManagerAccessor = timelineManagerAccessor;
    }
}
