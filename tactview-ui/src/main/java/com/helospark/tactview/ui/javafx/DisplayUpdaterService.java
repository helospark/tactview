package com.helospark.tactview.ui.javafx;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Generated;
import javax.annotation.PostConstruct;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.timeline.GlobalDirtyClipManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedListener;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedRequest;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

@Component
public class DisplayUpdaterService implements ScenePostProcessor {
    private static final int NUMBER_OF_CACHE_JOBS_TO_RUN = 2;
    private final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_CACHE_JOBS_TO_RUN,
            new ThreadFactoryBuilder().setNameFormat("playback-prefetch-cache-job-%d").build());
    private final Map<TimelinePosition, Future<JavaDisplayableAudioVideoFragment>> framecache = new ConcurrentHashMap<>();
    private volatile long currentPositionLastRendered = -1;

    private final PlaybackFrameAccessor playbackFrameAccessor;
    private final UiProjectRepository uiProjectRepostiory;
    private final UiTimelineManager uiTimelineManager;
    private final GlobalDirtyClipManager globalDirtyClipManager;
    private final List<DisplayUpdatedListener> displayUpdateListeners;
    private final MessagingService messagingService;
    private final ScheduledExecutorService scheduledExecutorService;

    private final boolean debugAudioUpdateEnabled;

    // cache current frame
    private Image cacheCurrentImage;
    private long cacheLastModifiedTime;
    private TimelinePosition cachePosition;

    // end of current frame cache

    private final Queue<TimelinePosition> recentlyDroppedFrames = new CircularFifoQueue<>(4);

    @Slf4j
    private Logger logger;

    private Canvas canvas;

    public DisplayUpdaterService(PlaybackFrameAccessor playbackController, UiProjectRepository uiProjectRepostiory, UiTimelineManager uiTimelineManager,
            GlobalDirtyClipManager globalDirtyClipManager, List<DisplayUpdatedListener> displayUpdateListeners, MessagingService messagingService,
            @Value("${debug.display-audio-updater.enabled}") boolean debugAudioUpdateEnabled,
            @Qualifier("generalTaskScheduledService") ScheduledExecutorService scheduledExecutorService) {
        this.playbackFrameAccessor = playbackController;
        this.uiProjectRepostiory = uiProjectRepostiory;
        this.uiTimelineManager = uiTimelineManager;
        this.globalDirtyClipManager = globalDirtyClipManager;
        this.displayUpdateListeners = displayUpdateListeners;
        this.messagingService = messagingService;
        this.debugAudioUpdateEnabled = debugAudioUpdateEnabled;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @PostConstruct
    public void init() {
        messagingService.register(DisplayUpdateRequestMessage.class, message -> {
            if (message.isInvalidateCache()) {
                updateCurrentPositionWithInvalidatedCache();
            } else {
                updateCurrentPositionWithoutInvalidatedCache();
            }
        });
        messagingService.register(AffectedModifiedIntervalAware.class, message -> {
            // this could be optimized based on the affected interval
            framecache.clear();
        });
    }

    @Override
    public void postProcess(Scene scene) {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                TimelinePosition currentPosition = uiTimelineManager.getCurrentPosition();
                long currentPostionLastModified = globalDirtyClipManager.positionLastModified(currentPosition);
                if (currentPostionLastModified > currentPositionLastRendered) {
                    updateCurrentPositionWithInvalidatedCache();
                    logger.debug("Current position changed, updating {}", currentPosition);
                }
            } catch (Exception e) {
                logger.warn("Unable to check dirty state of display", e);
            }
        }, 200, 200, TimeUnit.MILLISECONDS);
    }

    // TODO: something is really not right here
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public void updateCurrentPositionWithInvalidatedCache() {
        cacheCurrentImage = null;
        updateDisplay(uiTimelineManager.getCurrentPosition());
    }

    public void updateCurrentPositionWithoutInvalidatedCache() {
        cacheCurrentImage = null;
        updateDisplay(uiTimelineManager.getCurrentPosition());
    }

    public void updateDisplayWithCacheInvalidation(TimelinePosition currentPosition) {
        cacheCurrentImage = null;
        updateDisplay(currentPosition);
    }

    int numberOfDroppedFrames = 0;

    public void updateDisplayAsync(TimelineDisplayAsyncUpdateRequest request) {
        TimelinePosition currentPosition = request.currentPosition;
        JavaDisplayableAudioVideoFragment actualAudioVideoFragment = null;
        Future<JavaDisplayableAudioVideoFragment> cachedKey = framecache.remove(currentPosition);

        if (cachedKey == null || !cachedKey.isDone()) {
            actualAudioVideoFragment = null;
            ++numberOfDroppedFrames;
            recentlyDroppedFrames.add(currentPosition);

            if (cachedKey == null) {
                logger.info("Cachekey is missing");
            } else {
                logger.info("Cachekey is present, but work is not finished");
            }
        } else {
            logger.info("Frame is loading from cache");
            actualAudioVideoFragment = getValueFromCache(cachedKey);
        }

        if (actualAudioVideoFragment == null && (numberOfDroppedFrames > 10 || !request.canDropFrames)) {
            logger.info("Frame would be dropped, but updating anyway, this will cause pop in sound");
            if (cachedKey != null) {
                actualAudioVideoFragment = getValueFromCache(cachedKey);
            } else {
                actualAudioVideoFragment = playbackFrameAccessor.getVideoFrameAt(currentPosition);
            }

            numberOfDroppedFrames = 0;
        }

        if (actualAudioVideoFragment != null) {
            displayFrameAsync(currentPosition, 0, actualAudioVideoFragment);
            numberOfDroppedFrames = 0;
        }
        startCacheJobs(request.expectedNextPositions);
        currentPositionLastRendered = System.currentTimeMillis();
        logger.debug("Rendered at {} dropped frames {}", currentPosition, numberOfDroppedFrames);
    }

    protected JavaDisplayableAudioVideoFragment getValueFromCache(Future<JavaDisplayableAudioVideoFragment> cachedKey) {
        try {
            return cachedKey.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateDisplay(TimelinePosition currentPosition) {
        long currentPostionLastModified = globalDirtyClipManager.positionLastModified(currentPosition);
        JavaDisplayableAudioVideoFragment actualAudioVideoFragment;
        if (cacheCurrentImage != null && currentPosition.equals(cachePosition) && cacheLastModifiedTime == currentPostionLastModified) {
            actualAudioVideoFragment = new JavaDisplayableAudioVideoFragment(cacheCurrentImage, null);
        } else {
            Future<JavaDisplayableAudioVideoFragment> cachedKey = framecache.remove(currentPosition);
            if (cachedKey == null) {
                actualAudioVideoFragment = playbackFrameAccessor.getVideoFrameAt(currentPosition);
            } else {
                actualAudioVideoFragment = getValueFromCache(cachedKey);
            }
            currentPositionLastRendered = System.currentTimeMillis();
            logger.debug("Rendered at {}", currentPositionLastRendered);
        }
        displayFrameAsync(currentPosition, currentPostionLastModified, actualAudioVideoFragment);
        //        startCacheJobs(request.);
    }

    protected void displayFrameAsync(TimelinePosition currentPosition, long currentPostionLastModified, JavaDisplayableAudioVideoFragment actualAudioVideoFragment) {
        Platform.runLater(() -> {
            try {
                int width = uiProjectRepostiory.getPreviewWidth();
                int height = uiProjectRepostiory.getPreviewHeight();
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                Image image = actualAudioVideoFragment.getImage();
                gc.drawImage(image, 0, 0, width, height);

                DisplayUpdatedRequest displayUpdateRequest = DisplayUpdatedRequest.builder()
                        .withImage(image)
                        .withPosition(currentPosition)
                        .withGraphics(gc)
                        .withCanvas(canvas)
                        .build();
                cacheCurrentImage = image;
                cachePosition = currentPosition;
                cacheLastModifiedTime = currentPostionLastModified;

                displayUpdateListeners.stream()
                        .forEach(a -> a.displayUpdated(displayUpdateRequest));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public TimelinePosition getLastDisplayedImageTimestamp() {
        return cachePosition;
    }

    private void startCacheJobs(List<TimelinePosition> expectedNextFrames) {
        for (TimelinePosition nextFrameTime : expectedNextFrames) {
            int numberOfJobsAdded = 0;
            if (!framecache.containsKey(nextFrameTime)) {
                Future<JavaDisplayableAudioVideoFragment> task = executorService.submit(() -> {
                    logger.debug("started {}", nextFrameTime);
                    if (recentlyDroppedFrames.contains(nextFrameTime)) {
                        return null; // result is ignored, we dropped this frame
                    } else {
                        return playbackFrameAccessor.getVideoFrameAt(nextFrameTime);
                    }
                });
                framecache.put(nextFrameTime, task);
                ++numberOfJobsAdded;
                if (numberOfJobsAdded > NUMBER_OF_CACHE_JOBS_TO_RUN) {
                    break;
                }
            }
        }
    }

    public static class TimelineDisplayAsyncUpdateRequest {
        TimelinePosition currentPosition;
        List<TimelinePosition> expectedNextPositions;
        boolean canDropFrames;

        @Generated("SparkTools")
        private TimelineDisplayAsyncUpdateRequest(Builder builder) {
            this.currentPosition = builder.currentPosition;
            this.expectedNextPositions = builder.expectedNextPositions;
            this.canDropFrames = builder.canDropFrames;
        }

        @Generated("SparkTools")
        public static Builder builder() {
            return new Builder();
        }

        @Generated("SparkTools")
        public static final class Builder {
            private TimelinePosition currentPosition;
            private List<TimelinePosition> expectedNextPositions = Collections.emptyList();
            private boolean canDropFrames;

            private Builder() {
            }

            public Builder withCurrentPosition(TimelinePosition currentPosition) {
                this.currentPosition = currentPosition;
                return this;
            }

            public Builder withExpectedNextPositions(List<TimelinePosition> expectedNextPositions) {
                this.expectedNextPositions = expectedNextPositions;
                return this;
            }

            public Builder withCanDropFrames(boolean canDropFrames) {
                this.canDropFrames = canDropFrames;
                return this;
            }

            public TimelineDisplayAsyncUpdateRequest build() {
                return new TimelineDisplayAsyncUpdateRequest(this);
            }
        }

    }

}
