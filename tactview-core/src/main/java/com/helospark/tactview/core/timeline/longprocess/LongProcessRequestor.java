package com.helospark.tactview.core.timeline.longprocess;

import static com.helospark.tactview.core.timeline.longprocess.LongProcessDuplaceRequestStrategy.ONLY_KEEP_LATEST_REQUEST;
import static com.helospark.tactview.core.timeline.message.progress.ProgressType.LONG_PROCESS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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
import com.helospark.tactview.core.timeline.message.EffectRemovedMessage;
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

    private LinkedBlockingQueue<LongProcessDescriptor> requestedJobs = new LinkedBlockingQueue<>();
    private ConcurrentHashMap<String, LongProcessDescriptor> runningJobs = new ConcurrentHashMap<>();

    private volatile boolean running = true;

    public LongProcessRequestor(ProjectRepository projectRepository, MessagingService messagingService) {
        this.projectRepository = projectRepository;
        this.messagingService = messagingService;
    }

    @PostConstruct
    public void init() {
        new Thread(() -> {
            while (running) {
                try {
                    LongProcessDescriptor element = requestedJobs.poll(2, TimeUnit.SECONDS);
                    if (element != null) {
                        runningJobs.put(element.jobId, element);
                        CompletableFuture.runAsync(element.runnable, executor)
                                .exceptionally(e -> {
                                    e.printStackTrace();
                                    return null;
                                })
                                .thenAccept(result -> {
                                    runningJobs.remove(element.jobId);
                                });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "long-job-poller").start();

        messagingService.register(EffectRemovedMessage.class, message -> {
            removeAndStopAllJobsWithEffectId(message.getEffectId());
        });
    }

    @PreDestroy
    public void preDestroy() {
        running = false;
    }

    public <T extends StatelessVideoEffect & LongProcessVisualImagePushAware> void requestFrames(T target, LongProcessFrameRequest longProcessFrameRequest) {
        VisualTimelineClip clip = (VisualTimelineClip) timelineManagerAccessor.findClipForEffect(target.getId()).get();
        Optional<Integer> effectChannel = clip.getEffectChannelIndex(target.getId());

        LongProcessDescriptor descriptor = new LongProcessDescriptor();
        descriptor.setClipId(clip.getId());
        descriptor.setEffectId(Optional.of(target.getId()));
        descriptor.setJobId(UUID.randomUUID().toString());

        if (longProcessFrameRequest.getDuplaceRequestStrategy().equals(ONLY_KEEP_LATEST_REQUEST)) {
            removeAndStopAllJobsWithEffectId(target.getId());
        }

        Runnable runnable = () -> {
            String jobId = descriptor.jobId;
            try {
                TimelineInterval interval = target.getGlobalInterval();
                BigDecimal step = BigDecimal.ONE.divide(projectRepository.getFps(), 10, RoundingMode.HALF_UP);
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
        descriptor.setRunnable(runnable);

        requestedJobs.add(descriptor);
    }

    private <T extends StatelessVideoEffect & LongProcessVisualImagePushAware> void removeAndStopAllJobsWithEffectId(String effectId) {
        List<LongProcessDescriptor> elementsToRemove = requestedJobs.stream()
                .filter(job -> job.effectId.orElse("").equals(effectId))
                .collect(Collectors.toList());
        elementsToRemove.stream()
                .forEach(e -> requestedJobs.remove(e));
        runningJobs.values()
                .stream()
                .filter(a -> a.effectId.orElse("").equals(effectId))
                .forEach(a -> a.setAborted(true));
    }

    // Due to circular dependencies. TODO: avoid
    public void setTimelineManagerAccessor(TimelineManagerAccessor timelineManagerAccessor) {
        this.timelineManagerAccessor = timelineManagerAccessor;
    }

    public Map<String, LongProcessDescriptor> getRunningJobs() {
        return runningJobs;
    }

    public boolean isFinished() {
        return requestedJobs.isEmpty() && runningJobs.isEmpty();
    }

}
