package com.helospark.tactview.core.timeline.longprocess;

import static com.helospark.tactview.core.timeline.longprocess.LongProcessDuplaceRequestStrategy.ONLY_KEEP_LATEST_REQUEST;

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
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.audioeffect.StatelessAudioEffect;
import com.helospark.tactview.core.timeline.longprocess.factory.AudioLongProcessRunnableFactory;
import com.helospark.tactview.core.timeline.longprocess.factory.VisualLongProcessRunnableFactory;
import com.helospark.tactview.core.timeline.message.EffectRemovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class LongProcessRequestor {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private MessagingService messagingService;
    private TimelineManagerAccessor timelineManagerAccessor;
    private VisualLongProcessRunnableFactory visualLongProcessRunnableFactory;
    private AudioLongProcessRunnableFactory audioLongProcessRunnableFactory;

    private LinkedBlockingQueue<LongProcessDescriptor> requestedJobs = new LinkedBlockingQueue<>();
    private ConcurrentHashMap<String, LongProcessDescriptor> runningJobs = new ConcurrentHashMap<>();

    private volatile boolean running = true;

    public LongProcessRequestor(MessagingService messagingService, VisualLongProcessRunnableFactory visualLongProcessRunnableFactory,
            AudioLongProcessRunnableFactory audioLongProcessRunnableFactory) {
        this.messagingService = messagingService;
        this.visualLongProcessRunnableFactory = visualLongProcessRunnableFactory;
        this.audioLongProcessRunnableFactory = audioLongProcessRunnableFactory;
    }

    @PostConstruct
    public void init() {
        new Thread(() -> {
            while (running) {
                try {
                    LongProcessDescriptor element = requestedJobs.poll(2, TimeUnit.SECONDS);
                    if (element != null) {
                        CompletableFuture.runAsync(() -> {
                            runningJobs.put(element.jobId, element);
                            element.runnable.run();
                        }, executor)
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

        Runnable runnable = visualLongProcessRunnableFactory.createVisualRunnable(target, clip, effectChannel, descriptor);
        descriptor.setRunnable(runnable);

        requestedJobs.add(descriptor);
    }

    public <T extends StatelessAudioEffect & LongProcessAudibleImagePushAware> void requestAudioFrames(T target, LongProcessFrameRequest longProcessFrameRequest) {
        AudibleTimelineClip clip = (AudibleTimelineClip) timelineManagerAccessor.findClipForEffect(target.getId()).get();
        Optional<Integer> effectChannel = clip.getEffectChannelIndex(target.getId());

        LongProcessDescriptor descriptor = new LongProcessDescriptor();
        descriptor.setClipId(clip.getId());
        descriptor.setEffectId(Optional.of(target.getId()));
        descriptor.setJobId(UUID.randomUUID().toString());

        if (longProcessFrameRequest.getDuplaceRequestStrategy().equals(ONLY_KEEP_LATEST_REQUEST)) {
            removeAndStopAllJobsWithEffectId(target.getId());
        }

        Runnable runnable = audioLongProcessRunnableFactory.createAudibleRunnable(target, clip, effectChannel, descriptor);
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
