package com.helospark.tactview.ui.javafx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.application.Platform;

@Component
public class UiTimelineManager {
    private static final int NUMBER_OF_FRAMES_TO_PRECACHE = 20;
    // private IntegerProperty timelinePosition = new SimpleIntegerProperty(0);
    private List<Consumer<TimelinePosition>> uiPlaybackConsumers = new ArrayList<>();
    private List<Consumer<TimelinePosition>> playbackConsumers = new ArrayList<>();
    private List<Consumer<PlaybackStatus>> statusChangeConsumers = new ArrayList<>();
    //    private double fps = 30;
    //    private long sleepTime = (long) (1 / fps * 1000);
    //    private BigDecimal increment = new BigDecimal(1).divide(new BigDecimal(fps), 100, RoundingMode.HALF_DOWN);

    private volatile TimelinePosition currentPosition = new TimelinePosition(BigDecimal.ZERO);
    private volatile boolean isPlaying;
    private Thread runThread;
    private Object timelineLock = new Object();

    private ProjectRepository projectRepository;

    public UiTimelineManager(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void registerUiPlaybackConsumer(Consumer<TimelinePosition> consumer) {
        this.uiPlaybackConsumers.add(consumer);
    }

    public void registerPlaybackConsumer(Consumer<TimelinePosition> consumer) {
        this.playbackConsumers.add(consumer);
    }

    public void registerStoppedConsumer(Consumer<PlaybackStatus> consumer) {
        this.statusChangeConsumers.add(consumer);
    }

    public void startPlayback() {
        BigDecimal fps = projectRepository.getFps();
        long sleepTime = BigDecimal.ONE.divide(fps, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(1000)).longValue();
        BigDecimal increment = getIncrement();

        if (!isPlaying) {
            isPlaying = true;
            statusChangeConsumers.stream()
                    .forEach(consumer -> consumer.accept(PlaybackStatus.STARTED));
            runThread = new Thread(() -> {
                while (isPlaying) {
                    synchronized (timelineLock) {
                        currentPosition = currentPosition.add(increment);
                    }
                    notifyConsumers();
                    sleep(sleepTime);
                }
            }, "playback-thread");
            runThread.start();
        }
    }

    public void stopPlayback() {
        if (isPlaying) {
            isPlaying = false;
            statusChangeConsumers.stream()
                    .forEach(consumer -> consumer.accept(PlaybackStatus.STOPPED));
        }
    }

    public void jumpRelative(BigDecimal seconds) {
        synchronized (timelineLock) {
            currentPosition = currentPosition.add(seconds);
            if (currentPosition.isLessThan(0)) {
                currentPosition = TimelinePosition.ofZero();
            }
        }
        notifyConsumers();
    }

    public void jumpAbsolute(BigDecimal seconds) {
        synchronized (timelineLock) {
            currentPosition = new TimelinePosition(seconds);
        }
        notifyConsumers();
    }

    private void sleep(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyConsumers() {
        for (var consumer : playbackConsumers) {
            consumer.accept(currentPosition);
        }
        for (var consumer : uiPlaybackConsumers) {
            Platform.runLater(() -> consumer.accept(currentPosition));
        }
    }

    public TimelinePosition getCurrentPosition() {
        return currentPosition;
    }

    public List<TimelinePosition> expectedNextFrames() {
        return expectedNextFrames(NUMBER_OF_FRAMES_TO_PRECACHE);

    }

    public List<TimelinePosition> expectedNextFrames(int number) {
        BigDecimal increment = getIncrement();
        if (isPlaying) {
            List<TimelinePosition> result = new ArrayList<>();
            TimelinePosition position = currentPosition;
            for (int i = 0; i < number; ++i) {
                position = position.add(increment);
                result.add(position);
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    public void moveBackOneFrame() {
        jumpRelative(getIncrement().negate());
    }

    public void moveForwardOneFrame() {
        jumpRelative(getIncrement());
    }

    public BigDecimal getIncrement() {
        BigDecimal fps = projectRepository.getFps();
        return new BigDecimal(1).divide(fps, 100, RoundingMode.HALF_DOWN);
    }

    public boolean isPlaybackInProgress() {
        return isPlaying;
    }

    public static enum PlaybackStatus {
        STARTED,
        STOPPED
    }
}
