package com.helospark.tactview.ui.javafx;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.audio.AudioStreamService;
import com.helospark.tactview.ui.javafx.uicomponents.audiocomponent.AudioVisualizationComponent;

@Component
public class AudioUpdaterService {
    private static final int AUDIOFRAME_NUMBER_PER_ELEMENT = 15;

    private UiTimelineManager uiTimelineManager;
    private PlaybackController playbackController;
    private AudioStreamService audioStreamService;
    private MessagingService messagingService;
    private AudioVisualizationComponent audioVisualizationComponent;

    private LinkedHashMap<BigDecimal, AudioData> buffer = new LinkedHashMap<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    public AudioUpdaterService(UiTimelineManager uiTimelineManager, PlaybackController playbackController, AudioStreamService audioStreamService, MessagingService messagingService,
            AudioVisualizationComponent audioVisualizationComponent) {
        this.uiTimelineManager = uiTimelineManager;
        this.playbackController = playbackController;
        this.audioStreamService = audioStreamService;
        this.messagingService = messagingService;
        this.audioVisualizationComponent = audioVisualizationComponent;
    }

    @PostConstruct
    public void init() {
        messagingService.register(AffectedModifiedIntervalAware.class, message -> {
            buffer.clear(); // TODO: finer control here
        });
    }

    public void updateAtPosition(TimelinePosition position) {
        if (uiTimelineManager.isPlaybackInProgress()) {
            BigDecimal normalizedStartPosition = normalizePosition(position);

            AudioData currentData = buffer.get(normalizedStartPosition);
            if (currentData != null && currentData.hasData()) {
                byte[] bytes = currentData.get();
                audioStreamService.streamAudio(bytes);
                audioVisualizationComponent.updateAudioComponent(position);
            } else {
                System.out.println("Key " + currentData + " " + " is not ready at " + normalizedStartPosition);
            }

            for (int i = 0; i < 100; i += AUDIOFRAME_NUMBER_PER_ELEMENT) {
                BigDecimal nextPosition = normalizedStartPosition.add(uiTimelineManager.getIncrement().multiply(BigDecimal.valueOf(i)));
                TimelinePosition timelinePosition = new TimelinePosition(nextPosition);
                if (!buffer.containsKey(nextPosition)) {
                    System.out.println("Starting: " + nextPosition);
                    Future<byte[]> future = executorService.submit(() -> {
                        return playbackController.getAudioFrameAt(timelinePosition, AUDIOFRAME_NUMBER_PER_ELEMENT);
                    });
                    buffer.put(nextPosition, new AudioData(timelinePosition, future));
                }
            }

            buffer.entrySet()
                    .removeIf(e -> e.getKey().compareTo(normalizedStartPosition) < 0);
        } else {
            audioVisualizationComponent.clearCanvas();
        }
    }

    private BigDecimal normalizePosition(TimelinePosition position) {
        return position.getSeconds().subtract(position.getSeconds().remainder(uiTimelineManager.getIncrement()));
    }

    public void playbackStopped() {
        audioStreamService.clearBuffer();
    }

    static class AudioData {
        TimelinePosition startPosition;
        Future<byte[]> futureData;

        public AudioData(TimelinePosition position, TimelinePosition endPosition, CompletableFuture<byte[]> futureData) {
            this.startPosition = position;
            this.futureData = futureData;
        }

        public byte[] get() {
            try {
                return futureData.get();
            } catch (Exception e) {
                return new byte[0];
            }
        }

        public boolean hasData() {
            return futureData.isDone();
        }

        public AudioData(TimelinePosition position, Future<byte[]> future) {
            this.startPosition = position;
            this.futureData = future;
        }

        @Override
        public String toString() {
            return "AudioData [startPosition=" + startPosition + "]";
        }

    }

}
