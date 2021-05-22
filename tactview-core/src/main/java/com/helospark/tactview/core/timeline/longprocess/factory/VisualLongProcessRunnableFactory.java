package com.helospark.tactview.core.timeline.longprocess.factory;

import static com.helospark.tactview.core.timeline.message.progress.ProgressType.LONG_PROCESS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.longprocess.LongProcessDescriptor;
import com.helospark.tactview.core.timeline.longprocess.LongProcessImagePushRequest;
import com.helospark.tactview.core.timeline.longprocess.LongProcessVisualImagePushAware;
import com.helospark.tactview.core.timeline.message.progress.ProgressAdvancedMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressDoneMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressInitializeMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class VisualLongProcessRunnableFactory {
    private MessagingService messagingService;
    private ProjectRepository projectRepository;

    public VisualLongProcessRunnableFactory(MessagingService messagingService, ProjectRepository projectRepository) {
        this.messagingService = messagingService;
        this.projectRepository = projectRepository;
    }

    public <T extends StatelessVideoEffect & LongProcessVisualImagePushAware> Runnable createVisualRunnable(T target, VisualTimelineClip clip, Optional<Integer> effectChannel,
            LongProcessDescriptor descriptor) {
        return () -> {
            String jobId = descriptor.jobId;
            try {
                TimelineInterval interval = target.getGlobalInterval();
                BigDecimal step = projectRepository.getFrameTime();
                messagingService.sendAsyncMessage(new ProgressInitializeMessage(jobId, interval.getLength().getSeconds().divide(step, 10, RoundingMode.HALF_UP).intValue(), LONG_PROCESS));

                TimelinePosition currentPosition = interval.getStartPosition();

                target.beginToPushLongImages();

                while (currentPosition.isLessThan(interval.getEndPosition()) && !descriptor.isAborted()) {
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
                if (!descriptor.isAborted()) {
                    target.endToPushLongImages();
                } else {
                    target.abortedLongImagePush();
                }
                messagingService.sendAsyncMessage(new ProgressDoneMessage(jobId));
            }
        };
    }

}
