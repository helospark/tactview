package com.helospark.tactview.ui.javafx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.application.Platform;

@Component
public class GlobalTimelinePositionHolder {
    private volatile TimelinePosition currentPosition = new TimelinePosition(BigDecimal.ZERO);
    private final List<Consumer<TimelinePosition>> uiPlaybackConsumers = new ArrayList<>();
    private final Object timelineLock = new Object();

    private ProjectRepository projectRepository;

    public GlobalTimelinePositionHolder(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public TimelinePosition getCurrentPosition() {
        return currentPosition;
    }

    public void jumpAbsolute(BigDecimal seconds) {
        if (seconds.compareTo(BigDecimal.ZERO) < 0) {
            seconds = BigDecimal.ZERO;
        }
        BigDecimal frameTime = projectRepository.getFrameTime();

        seconds = seconds.divideToIntegralValue(frameTime).multiply(frameTime);

        synchronized (timelineLock) {
            currentPosition = new TimelinePosition(seconds);
        }
        notifyConsumers();
    }

    public void jumpAbsolute(TimelinePosition seconds) {
        jumpAbsolute(seconds.getSeconds());
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

    public void registerUiPlaybackConsumer(Consumer<TimelinePosition> consumer) {
        this.uiPlaybackConsumers.add(consumer);
    }

    private void notifyConsumers() {
        for (var consumer : uiPlaybackConsumers) {
            Platform.runLater(() -> consumer.accept(currentPosition));
        }
    }
}
