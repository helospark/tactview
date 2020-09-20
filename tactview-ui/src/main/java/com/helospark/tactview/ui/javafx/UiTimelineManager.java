package com.helospark.tactview.ui.javafx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.preference.PreferenceValue;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.DisplayUpdaterService.TimelineDisplayAsyncUpdateRequest;
import com.helospark.tactview.ui.javafx.audio.AudioStreamService;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;

import javafx.application.Platform;

@Component
public class UiTimelineManager {
    private final int CACHE_MODULO = 1;
    private int numberOfFramesToCache = 2;
    private final List<Consumer<TimelinePosition>> uiPlaybackConsumers = new ArrayList<>();
    private final List<Consumer<TimelinePosition>> playbackConsumers = new ArrayList<>();
    private final List<Consumer<PlaybackStatus>> statusChangeConsumers = new ArrayList<>();

    private volatile TimelinePosition currentPosition = new TimelinePosition(BigDecimal.ZERO);
    private volatile boolean isPlaying;
    private Thread runThread;
    private final Object timelineLock = new Object();

    private final ProjectRepository projectRepository;
    private final TimelineState timelineState;
    private final PlaybackController playbackController;
    private final AudioStreamService audioStreamService;
    private DisplayUpdaterService displayUpdaterService;

    public UiTimelineManager(ProjectRepository projectRepository, TimelineState timelineState, PlaybackController playbackController,
            AudioStreamService audioStreamService) {
        this.projectRepository = projectRepository;
        this.timelineState = timelineState;
        this.playbackController = playbackController;
        this.audioStreamService = audioStreamService;
    }

    public void setDisplayUpdaterService(DisplayUpdaterService displayUpdaterService) {
        this.displayUpdaterService = displayUpdaterService;
    }

    public void registerUiPlaybackConsumer(Consumer<TimelinePosition> consumer) {
        this.uiPlaybackConsumers.add(consumer);
    }

    public void registerStoppedConsumer(Consumer<PlaybackStatus> consumer) {
        this.statusChangeConsumers.add(consumer);
    }

    public void startPlayback() {
        if (!isPlaying) {
            isPlaying = true;
            statusChangeConsumers.stream()
                    .forEach(consumer -> consumer.accept(PlaybackStatus.STARTED));
            runThread = new Thread(() -> {
                try {
                    int frame = 0;
                    while (isPlaying) {
                        System.out.println("Playing frame " + frame);
                        TimelinePosition nextFrame = this.expectedNextFrames(1).get(0);
                        synchronized (timelineLock) {
                            currentPosition = nextFrame;
                        }

                        byte[] audioFrame = playbackController.getAudioFrameAt(currentPosition, 1);

                        audioStreamService.streamAudio(audioFrame);

                        // finished writing currentPosition to buffer, play this frame

                        TimelineDisplayAsyncUpdateRequest request = TimelineDisplayAsyncUpdateRequest.builder()
                                .withCanDropFrames(true)
                                .withCurrentPosition(currentPosition)
                                .withExpectedNextPositions(expectedNextFramesWithDroppedFrameModulo(10, CACHE_MODULO, frame + 1))
                                .build();

                        if (frame % CACHE_MODULO == 0) {
                            displayUpdaterService.updateDisplayAsync(request);
                        }

                        notifyConsumers();
                        ++frame;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    isPlaying = false;
                }
            }, "playback-thread");
            runThread.start();
        }
    }

    public void refreshDisplay() {
        displayUpdaterService.updateDisplay(this.getCurrentPosition());
        notifyConsumers();
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
        refreshDisplay();
    }

    public void jumpAbsolute(BigDecimal seconds) {
        if (seconds.compareTo(BigDecimal.ZERO) < 0) {
            seconds = BigDecimal.ZERO;
        }
        synchronized (timelineLock) {
            currentPosition = new TimelinePosition(seconds);
        }
        refreshDisplay();
    }

    public void refresh() {
        refreshDisplay();
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
        return expectedNextFrames(numberOfFramesToCache);

    }

    public List<TimelinePosition> expectedNextFrames(int number) {
        return expectedNextFramesWithDroppedFrameModulo(number, 1, 0);
    }

    private List<TimelinePosition> expectedNextFramesWithDroppedFrameModulo(int number, int modulo, int startingFrame) {
        BigDecimal increment = getIncrement();
        if (isPlaying) {
            List<TimelinePosition> result = new ArrayList<>();
            TimelinePosition position = currentPosition;

            if (timelineState.loopingEnabled() && position.isLessThan(timelineState.getLoopStartTime())) {
                position = timelineState.getLoopStartTime();
            }

            while (result.size() < number) {
                position = position.add(increment);

                if (timelineState.loopingEnabled() && position.isGreaterThan(timelineState.getLoopEndTime())) {
                    position = timelineState.getLoopStartTime();
                }

                if (startingFrame % modulo == 0) {
                    result.add(position);
                }
                ++startingFrame;
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

    @PreferenceValue(name = "Number of frames to preload during playback", defaultValue = "2", group = "Performance")
    public void setImageClipLength(Integer numberOfFrames) {
        numberOfFramesToCache = numberOfFrames;
    }

    public static enum PlaybackStatus {
        STARTED,
        STOPPED
    }
}
