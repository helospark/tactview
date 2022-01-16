package com.helospark.tactview.ui.javafx;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.GlobalDirtyClipManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.TimelineRenderResult.RegularRectangle;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.PlaybackFrameAccessor.FrameSize;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.repository.selection.ClipSelectionChangedMessage;
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
    private static final int SELECTION_BOX_DASH_SIZE = 5;
    private final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_CACHE_JOBS_TO_RUN,
            new ThreadFactoryBuilder().setNameFormat("playback-prefetch-cache-job-%d").build());
    private final Map<TimelinePosition, Future<JavaDisplayableAudioVideoFragment>> framecache = new ConcurrentHashMap<>();
    private volatile long currentPositionLastRendered = -1;

    private final PlaybackFrameAccessor playbackFrameAccessor;
    private final UiProjectRepository uiProjectRepostiory;
    private final ProjectRepository projectRepository;
    private final UiTimelineManager uiTimelineManager;
    private final GlobalDirtyClipManager globalDirtyClipManager;
    private final List<DisplayUpdatedListener> displayUpdateListeners;
    private final MessagingService messagingService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final SelectedNodeRepository selectedNodeRepository;
    private final CanvasStateHolder canvasStateHolder;

    private FullScreenData fullscreenData = null;

    // cache current frame
    private JavaDisplayableAudioVideoFragment cacheCurrentImage;
    private long cacheLastModifiedTime;
    private TimelinePosition cachePosition;
    private volatile boolean hasSelectedElement = false;
    private volatile int selectionBoxUpdateCount = 0;

    // end of current frame cache

    private final Queue<TimelinePosition> recentlyDroppedFrames = new CircularFifoQueue<>(4);

    @Slf4j
    private Logger logger;

    private Canvas canvas;

    public DisplayUpdaterService(PlaybackFrameAccessor playbackController, UiProjectRepository uiProjectRepostiory, UiTimelineManager uiTimelineManager,
            GlobalDirtyClipManager globalDirtyClipManager, List<DisplayUpdatedListener> displayUpdateListeners, MessagingService messagingService,
            @Qualifier("generalTaskScheduledService") ScheduledExecutorService scheduledExecutorService, SelectedNodeRepository selectedNodeRepository,
            ProjectRepository projectRepository, CanvasStateHolder canvasStateHolder) {
        this.playbackFrameAccessor = playbackController;
        this.uiProjectRepostiory = uiProjectRepostiory;
        this.uiTimelineManager = uiTimelineManager;
        this.globalDirtyClipManager = globalDirtyClipManager;
        this.displayUpdateListeners = displayUpdateListeners;
        this.messagingService = messagingService;
        this.scheduledExecutorService = scheduledExecutorService;
        this.selectedNodeRepository = selectedNodeRepository;
        this.projectRepository = projectRepository;
        this.canvasStateHolder = canvasStateHolder;
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
        messagingService.register(ClipSelectionChangedMessage.class, message -> updateCurrentPositionWithoutInvalidatedCache());
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

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            ++selectionBoxUpdateCount;
            if (hasSelectedElement && canUseCacheAt(uiTimelineManager.getCurrentPosition())) {
                Platform.runLater(() -> drawSelectionRectangle(cacheCurrentImage, canvas.getGraphicsContext2D()));
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
                actualAudioVideoFragment = playbackFrameAccessor.getVideoFrameAt(currentPosition, getFrameSize());
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
        if (canUseCacheAt(currentPosition)) {
            actualAudioVideoFragment = cacheCurrentImage;
        } else {
            Future<JavaDisplayableAudioVideoFragment> cachedKey = framecache.remove(currentPosition);
            if (cachedKey == null) {
                actualAudioVideoFragment = playbackFrameAccessor.getVideoFrameAt(currentPosition, getFrameSize());
            } else {
                actualAudioVideoFragment = getValueFromCache(cachedKey);
            }
            currentPositionLastRendered = System.currentTimeMillis();
            logger.debug("Rendered at {}", currentPositionLastRendered);
        }
        displayFrameAsync(currentPosition, currentPostionLastModified, actualAudioVideoFragment);
        //        startCacheJobs(request.);
    }

    private boolean canUseCacheAt(TimelinePosition currentPosition) {
        return cacheCurrentImage != null && currentPosition.equals(cachePosition) && cacheLastModifiedTime == globalDirtyClipManager.positionLastModified(currentPosition);
    }

    protected void displayFrameAsync(TimelinePosition currentPosition, long currentPostionLastModified, JavaDisplayableAudioVideoFragment actualAudioVideoFragment) {
        Platform.runLater(() -> {
            try {
                boolean useDefaultCanvas = (fullscreenData == null);
                Canvas canvasToUse = (useDefaultCanvas ? canvas : fullscreenData.canvas);
                int width = (useDefaultCanvas ? uiProjectRepostiory.getPreviewWidth() : fullscreenData.width);
                int height = (useDefaultCanvas ? uiProjectRepostiory.getPreviewHeight() : fullscreenData.height);
                GraphicsContext gc = canvasToUse.getGraphicsContext2D();
                if (useDefaultCanvas) {
                    gc.clearRect(0, 0, canvasToUse.getWidth(), canvasToUse.getHeight());
                }
                double translateX = 0.0;
                double translateY = 0.0;

                if (useDefaultCanvas) {
                    translateX = canvasStateHolder.getTranslateX();
                    translateY = canvasStateHolder.getTranslateY();
                }

                gc.setFill(Color.BLACK);
                gc.fillRect(translateX, translateY, width, height);
                Image image = actualAudioVideoFragment.getImage();
                gc.drawImage(image, translateX, translateY, width, height);
                drawSelectionRectangle(actualAudioVideoFragment, gc);

                DisplayUpdatedRequest displayUpdateRequest = DisplayUpdatedRequest.builder()
                        .withImage(image)
                        .withPosition(currentPosition)
                        .withGraphics(gc)
                        .withCanvas(canvasToUse)
                        .build();
                cacheCurrentImage = actualAudioVideoFragment;
                cachePosition = currentPosition;
                cacheLastModifiedTime = currentPostionLastModified;

                displayUpdateListeners.stream()
                        .forEach(a -> a.displayUpdated(displayUpdateRequest));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void drawSelectionRectangle(JavaDisplayableAudioVideoFragment actualAudioVideoFragment, GraphicsContext gc) {
        if (actualAudioVideoFragment != null) {
            boolean foundSelection = false;
            for (var rectangle : actualAudioVideoFragment.getClipRectangle().entrySet()) {
                if (selectedNodeRepository.getSelectedClipIds().contains(rectangle.getKey())) {
                    RegularRectangle rect = rectangle.getValue();
                    drawRectangleWithDashedLine(gc, rect);
                    foundSelection = true;
                }
            }
            hasSelectedElement = foundSelection;
        }
    }

    private void drawRectangleWithDashedLine(GraphicsContext gc, RegularRectangle rect) {
        double translateX = canvasStateHolder.getTranslateX();
        double translateY = canvasStateHolder.getTranslateY();
        int boxOffset = selectionBoxUpdateCount;
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.0);
        gc.setLineDashOffset(boxOffset);
        gc.setLineDashes(SELECTION_BOX_DASH_SIZE, SELECTION_BOX_DASH_SIZE);
        gc.strokeRect(rect.getX() + translateX, rect.getY() + translateY, rect.getWidth(), rect.getHeight());

        gc.setStroke(Color.BLACK);
        gc.setLineDashOffset(boxOffset - SELECTION_BOX_DASH_SIZE);
        gc.strokeRect(rect.getX() + translateX, rect.getY() + translateY, rect.getWidth(), rect.getHeight());

        gc.setLineDashes(null);
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
                        return playbackFrameAccessor.getVideoFrameAt(nextFrameTime, getFrameSize());
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

    public JavaDisplayableAudioVideoFragment getCacheCurrentImage() {
        return cacheCurrentImage;
    }

    public void startFullscreenPreview(FullScreenData fullscreenData) {
        this.fullscreenData = fullscreenData;
    }

    public void stopFullscreenPreview() {
        this.fullscreenData = null;
    }

    private Optional<FrameSize> getFrameSize() {
        if (fullscreenData != null) {
            return Optional.ofNullable(new FrameSize(fullscreenData.width, fullscreenData.height, fullscreenData.scale));
        } else {
            return Optional.ofNullable(new FrameSize(uiProjectRepostiory.getPreviewWidth(), uiProjectRepostiory.getPreviewHeight(), uiProjectRepostiory.getScaleFactor()));
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

    static class FullScreenData {
        Canvas canvas;
        int width;
        int height;
        double scale;

        public FullScreenData(Canvas canvas, int width, int height, double scale) {
            this.canvas = canvas;
            this.width = width;
            this.height = height;
            this.scale = scale;
        }

    }

}
