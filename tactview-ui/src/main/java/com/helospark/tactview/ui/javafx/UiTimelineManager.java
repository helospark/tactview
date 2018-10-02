package com.helospark.tactview.ui.javafx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.application.Platform;

@Component
public class UiTimelineManager {
    private static final int NUMBER_OF_FRAMES_TO_PRECACHE = 2;
    //    private IntegerProperty timelinePosition = new SimpleIntegerProperty(0);
    private List<Consumer<TimelinePosition>> uiConsumers = new ArrayList<>();
    private List<Consumer<TimelinePosition>> consumers = new ArrayList<>();
    private double fps = 30;
    private long sleepTime = (long) (1 / fps * 1000);
    private BigDecimal increment = new BigDecimal(1).divide(new BigDecimal(fps), 3, RoundingMode.HALF_DOWN);

    private volatile TimelinePosition currentPosition = new TimelinePosition(BigDecimal.ZERO);
    private volatile boolean isPlaying;
    private Thread runThread;
    private Object timelineLock = new Object();

    public void registerUiConsumer(Consumer<TimelinePosition> consumer) {
        this.uiConsumers.add(consumer);
    }

    public void registerConsumer(Consumer<TimelinePosition> consumer) {
        this.consumers.add(consumer);
    }

    public void startPlayback() {
        if (!isPlaying) {
            isPlaying = true;
            runThread = new Thread(() -> {
                while (isPlaying) {
                    synchronized (timelineLock) {
                        currentPosition = currentPosition.add(increment);
                    }
                    notifyConsumers();
                    sleep(sleepTime);
                }
            });
            runThread.start();
        }
    }

    public void stopPlayback() {
        isPlaying = false;
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
        for (var consumer : consumers) {
            consumer.accept(currentPosition);
        }
        for (var consumer : uiConsumers) {
            Platform.runLater(() -> consumer.accept(currentPosition));
        }
    }

    public TimelinePosition getCurrentPosition() {
        return currentPosition;
    }

    public List<TimelinePosition> expectedNextFrames() {
        if (isPlaying) {
            List<TimelinePosition> result = new ArrayList<>();
            TimelinePosition position = currentPosition;
            for (int i = 0; i < NUMBER_OF_FRAMES_TO_PRECACHE; ++i) {
                position = position.add(increment);
                result.add(position);
            }
            return result;
        } else {
            return Collections.emptyList();
        }

    }

}
