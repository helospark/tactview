package com.helospark.tactview.ui.javafx;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
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
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private Map<TimelinePosition, Future<JavaDisplayableAudioVideoFragment>> framecache = new ConcurrentHashMap<>();
    private volatile long currentPositionLastRendered = -1;
    private volatile boolean running = true;

    private PlaybackController playbackController;
    private UiProjectRepository uiProjectRepostiory;
    private UiTimelineManager uiTimelineManager;
    private GlobalDirtyClipManager globalDirtyClipManager;
    private List<DisplayUpdatedListener> displayUpdateListeners;
    private MessagingService messagingService;

    // cache current frame
    private Image cacheCurrentImage;
    private long cacheLastModifiedTime;
    private TimelinePosition cachePosition;
    // end of current frame cache

    @Slf4j
    private Logger logger;

    private Canvas canvas;

    public DisplayUpdaterService(PlaybackController playbackController, UiProjectRepository uiProjectRepostiory, UiTimelineManager uiTimelineManager,
            GlobalDirtyClipManager globalDirtyClipManager, List<DisplayUpdatedListener> displayUpdateListeners, MessagingService messagingService) {
        this.playbackController = playbackController;
        this.uiProjectRepostiory = uiProjectRepostiory;
        this.uiTimelineManager = uiTimelineManager;
        this.globalDirtyClipManager = globalDirtyClipManager;
        this.displayUpdateListeners = displayUpdateListeners;
        this.messagingService = messagingService;
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

    public void updateDisplay(TimelinePosition currentPosition) {
        long currentPostionLastModified = globalDirtyClipManager.positionLastModified(currentPosition);
        JavaDisplayableAudioVideoFragment actualAudioVideoFragment;
        if (cacheCurrentImage != null && currentPosition.equals(cachePosition) && cacheLastModifiedTime == currentPostionLastModified) {
            actualAudioVideoFragment = new JavaDisplayableAudioVideoFragment(cacheCurrentImage, null);
        } else {
            Future<JavaDisplayableAudioVideoFragment> cachedKey = framecache.remove(currentPosition);
            if (cachedKey == null) {
                actualAudioVideoFragment = playbackController.getVideoFrameAt(currentPosition);
            } else {
                try {
                    actualAudioVideoFragment = cachedKey.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            currentPositionLastRendered = System.currentTimeMillis();
            logger.debug("Rendered at {}", currentPositionLastRendered);
        }
        Platform.runLater(() -> {
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
        });

        startCacheJobs(currentPosition);
    }

    private void startCacheJobs(TimelinePosition currentPosition) {
        List<TimelinePosition> expectedNextFrames = uiTimelineManager.expectedNextFrames();
        for (TimelinePosition nextFrameTime : expectedNextFrames) {
            if (!framecache.containsKey(nextFrameTime)) {
                Future<JavaDisplayableAudioVideoFragment> task = executorService.submit(() -> {
                    return playbackController.getVideoFrameAt(currentPosition);
                });
                framecache.put(nextFrameTime, task);
                System.out.println("started " + nextFrameTime);
            }
        }
    }

}
