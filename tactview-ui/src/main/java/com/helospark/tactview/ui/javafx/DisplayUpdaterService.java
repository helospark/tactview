package com.helospark.tactview.ui.javafx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.GlobalDirtyClipManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.logger.Slf4j;
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

@Component
public class DisplayUpdaterService implements ScenePostProcessor {
    private static final int NUMBER_OF_CACHE_JOBS_TO_RUN = 2;
    private final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_CACHE_JOBS_TO_RUN);
    private final Map<TimelinePosition, Future<JavaDisplayableAudioVideoFragment>> framecache = new ConcurrentHashMap<>();
    private volatile long currentPositionLastRendered = -1;
    private volatile boolean running = true;

    private final PlaybackController playbackController;
    private final UiProjectRepository uiProjectRepostiory;
    private final UiTimelineManager uiTimelineManager;
    private final GlobalDirtyClipManager globalDirtyClipManager;
    private final List<DisplayUpdatedListener> displayUpdateListeners;
    private final MessagingService messagingService;

    private final boolean debugAudioUpdateEnabled;

    // cache current frame
    private Image cacheCurrentImage;
    private long cacheLastModifiedTime;
    private TimelinePosition cachePosition;

    // end of current frame cache

    private final Queue<TimelinePosition> recentlyDroppedFrames = new CircularFifoQueue<>(4);
    private final Queue<Boolean> droppedOrNotDroppedFrame = new CircularFifoQueue<>(30);

    @Slf4j
    private Logger logger;

    private Canvas canvas;

    public DisplayUpdaterService(PlaybackController playbackController, UiProjectRepository uiProjectRepostiory, UiTimelineManager uiTimelineManager,
            GlobalDirtyClipManager globalDirtyClipManager, List<DisplayUpdatedListener> displayUpdateListeners, MessagingService messagingService,
            @Value("${debug.display-audio-updater.enabled}") boolean debugAudioUpdateEnabled) {
        this.playbackController = playbackController;
        this.uiProjectRepostiory = uiProjectRepostiory;
        this.uiTimelineManager = uiTimelineManager;
        this.globalDirtyClipManager = globalDirtyClipManager;
        this.displayUpdateListeners = displayUpdateListeners;
        this.messagingService = messagingService;
        this.debugAudioUpdateEnabled = debugAudioUpdateEnabled;
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
    }

    @Override
    public void postProcess(Scene scene) {
        Thread thread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(200);
                    TimelinePosition currentPosition = uiTimelineManager.getCurrentPosition();
                    long currentPostionLastModified = globalDirtyClipManager.positionLastModified(currentPosition);
                    if (currentPostionLastModified > currentPositionLastRendered) {
                        updateCurrentPositionWithInvalidatedCache();
                        logger.debug("Current position changed, updating {}", currentPosition);
                    }
                } catch (Exception e) {
                    logger.warn("Unable to check dirty state of display", e);
                }
            }
        }, "display-updater-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @PreDestroy
    public void destroy() {
        running = false;
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

    public void updateDisplayAsync(TimelinePosition currentPosition) {
        JavaDisplayableAudioVideoFragment actualAudioVideoFragment = null;
        Future<JavaDisplayableAudioVideoFragment> cachedKey = framecache.get(currentPosition);

        if (cachedKey == null || !cachedKey.isDone()) {
            actualAudioVideoFragment = null;
            ++numberOfDroppedFrames;
            recentlyDroppedFrames.add(currentPosition);
        } else {
            actualAudioVideoFragment = getValueFromCache(cachedKey);
        }

        if (actualAudioVideoFragment == null && numberOfDroppedFrames > 10) {
            logger.info("There were " + numberOfDroppedFrames + " consecutive frame dropped, blocking sound until frame loads");
            if (cachedKey != null) {
                actualAudioVideoFragment = getValueFromCache(cachedKey);
            } else {
                actualAudioVideoFragment = playbackController.getVideoFrameAt(currentPosition);
            }

            if (debugAudioUpdateEnabled) { // just so it is easier to debug. Will need to think of other solution later
                AudioVideoFragment result = playbackController.getSingleAudioFrameAtPosition(currentPosition);
                result.free();
            }
            numberOfDroppedFrames = 0;
        }

        if (actualAudioVideoFragment != null) {
            displayFrameAsync(currentPosition, 0, actualAudioVideoFragment);
            numberOfDroppedFrames = 0;
            droppedOrNotDroppedFrame.add(false);
        } else {
            if (cachedKey != null) {
                droppedOrNotDroppedFrame.add(true);
            } else {
                droppedOrNotDroppedFrame.add(false);
            }
        }
        startCacheJobs(currentPosition);
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
                actualAudioVideoFragment = playbackController.getVideoFrameAt(currentPosition);

                if (debugAudioUpdateEnabled) { // just so it is easier to debug. Will need to think of other solution later
                    AudioVideoFragment result = playbackController.getSingleAudioFrameAtPosition(currentPosition);
                    result.free();
                }
            } else {
                actualAudioVideoFragment = getValueFromCache(cachedKey);
            }
            currentPositionLastRendered = System.currentTimeMillis();
            logger.debug("Rendered at {}", currentPositionLastRendered);
        }
        displayFrameAsync(currentPosition, currentPostionLastModified, actualAudioVideoFragment);
        startCacheJobs(currentPosition);
    }

    protected void displayFrameAsync(TimelinePosition currentPosition, long currentPostionLastModified, JavaDisplayableAudioVideoFragment actualAudioVideoFragment) {
        Platform.runLater(() -> {
            try {
                int width = uiProjectRepostiory.getPreviewWidth();
                int height = uiProjectRepostiory.getPreviewHeight();
                GraphicsContext gc = canvas.getGraphicsContext2D();
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

    private void startCacheJobs(TimelinePosition currentPosition) {
        List<TimelinePosition> expectedNextFrames = uiTimelineManager.expectedNextFrames(5);

        int allDroppedFrames = droppedOrNotDroppedFrame.stream()
                .mapToInt(a -> a ? 1 : 0)
                .sum();
        int framesToPreempitvelyDrop = allDroppedFrames / 5;

        if (framesToPreempitvelyDrop > 0) {
            List<TimelinePosition> preemprivelyDroppedFramesCache = new ArrayList<>();
            for (int i = 0; i < expectedNextFrames.size(); ++i) {
                int current = i;
                while (i < current + framesToPreempitvelyDrop && i < expectedNextFrames.size()) {
                    logger.debug("Preempitevely dropping frame " + expectedNextFrames.get(i));
                    ++i;
                }
                if (i < expectedNextFrames.size()) {
                    preemprivelyDroppedFramesCache.add(expectedNextFrames.get(i));
                }
            }
            expectedNextFrames = preemprivelyDroppedFramesCache;
        }
        for (TimelinePosition nextFrameTime : expectedNextFrames) {
            int numberOfJobsAdded = 0;
            if (!framecache.containsKey(nextFrameTime)) {
                Future<JavaDisplayableAudioVideoFragment> task = executorService.submit(() -> {
                    if (recentlyDroppedFrames.contains(currentPosition)) {
                        return null; // result is ignored, we dropped this frame
                    } else {
                        return playbackController.getVideoFrameAt(currentPosition);
                    }
                });
                framecache.put(nextFrameTime, task);
                logger.debug("started " + nextFrameTime);
                ++numberOfJobsAdded;
                if (numberOfJobsAdded > NUMBER_OF_CACHE_JOBS_TO_RUN) {
                    break;
                }
            }
        }
    }

}
