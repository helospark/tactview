package com.helospark.tactview.core.timeline.longprocess.factory;

import static com.helospark.tactview.core.timeline.message.progress.ProgressType.LONG_PROCESS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.AudioRequest;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.audioeffect.StatelessAudioEffect;
import com.helospark.tactview.core.timeline.longprocess.LongProcessAudibleImagePushAware;
import com.helospark.tactview.core.timeline.longprocess.LongProcessAudioPushRequest;
import com.helospark.tactview.core.timeline.longprocess.LongProcessDescriptor;
import com.helospark.tactview.core.timeline.message.progress.ProgressAdvancedMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressDoneMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressInitializeMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class AudioLongProcessRunnableFactory {
    private MessagingService messagingService;
    private ProjectRepository projectRepository;

    public AudioLongProcessRunnableFactory(MessagingService messagingService, ProjectRepository projectRepository) {
        this.messagingService = messagingService;
        this.projectRepository = projectRepository;
    }

    public <T extends StatelessAudioEffect & LongProcessAudibleImagePushAware> Runnable createAudibleRunnable(T target, AudibleTimelineClip clip, Optional<Integer> effectChannel,
            LongProcessDescriptor descriptor) {
        return () -> {
            String jobId = descriptor.jobId;
            try {
                TimelineInterval interval = target.getGlobalInterval();
                BigDecimal step = BigDecimal.ONE.divide(projectRepository.getFps(), 10, RoundingMode.HALF_UP);
                messagingService.sendAsyncMessage(new ProgressInitializeMessage(jobId, interval.getLength().getSeconds().divide(step, 10, RoundingMode.HALF_UP).intValue(), LONG_PROCESS));

                TimelinePosition currentPosition = interval.getStartPosition();

                target.beginLongPush();

                while (currentPosition.isLessThan(interval.getEndPosition()) && !descriptor.isAborted()) {
                    AudioRequest frameRequest = AudioRequest.builder()
                            .withApplyEffects(true)
                            // effect channel
                            .withBytesPerSample(projectRepository.getBytesPerSample())
                            .withLength(new TimelineLength(projectRepository.getFrameTime()))
                            .withNumberOfChannels(projectRepository.getNumberOfChannels())
                            .withPosition(currentPosition)
                            .withSampleRate(projectRepository.getSampleRate())
                            .withEvaluationContext(null) // TODO: fix
                            .build();

                    AudioFrameResult frameResult = clip.requestAudioFrame(frameRequest);

                    LongProcessAudioPushRequest pushRequest = LongProcessAudioPushRequest.builder()
                            .withFrame(frameResult)
                            .withPosition(currentPosition)
                            .build();

                    target.longProcessImage(pushRequest);

                    messagingService.sendAsyncMessage(new ProgressAdvancedMessage(jobId, 1));

                    currentPosition = currentPosition.add(step);
                    frameResult.getChannels()
                            .stream()
                            .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a));
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
