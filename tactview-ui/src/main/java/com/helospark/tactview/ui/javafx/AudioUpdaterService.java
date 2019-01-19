package com.helospark.tactview.ui.javafx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.audio.AudioStreamService;

@Component
public class AudioUpdaterService {
    private static final int AUDIOFRAME_NUMBER_PER_ELEMENT = 5;
    private static final int MAX_RING_BUFFER_SIZE = 15;
    private static final int BYTES_PER_SAMPLE = 2 * 1;
    private static final BigDecimal BYTES_PER_SECOND = BigDecimal.valueOf(44100 * BYTES_PER_SAMPLE);

    private UiTimelineManager uiTimelineManager;
    private PlaybackController playbackController;
    private AudioStreamService audioStreamService;
    private ProjectRepository projectRepository;

    private Deque<AudioData> buffer = new LinkedList<>();
    private AtomicInteger inprogressItems = new AtomicInteger(0);

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    private TimelinePosition lastWrittenEndPosition = null;
    private volatile TimelinePosition latestPosition;

    public AudioUpdaterService(UiTimelineManager uiTimelineManager, PlaybackController playbackController, AudioStreamService audioStreamService, ProjectRepository projectRepository) {
        this.uiTimelineManager = uiTimelineManager;
        this.playbackController = playbackController;
        this.audioStreamService = audioStreamService;
        this.projectRepository = projectRepository;
    }

    public void updateAtPosition(TimelinePosition position) {
        if (uiTimelineManager.isPlaybackInProgress()) {
            latestPosition = position;

            if (lastWrittenEndPosition == null || position.isGreaterThan(lastWrittenEndPosition)) {
                streamAudio(position);
            } else if (lastWrittenEndPosition.subtract(position).getSeconds().doubleValue() * 1000.0 < 50.0) {
                streamAudio(lastWrittenEndPosition);
            }

            TimelinePosition fillBuffersFrom = lastWrittenEndPosition != null ? lastWrittenEndPosition : position;
            int endIndex = MAX_RING_BUFFER_SIZE - buffer.size() - inprogressItems.get();
            for (int i = 0; i < endIndex; ++i) {
                int currentIndex = i;
                inprogressItems.incrementAndGet();
                TimelinePosition startPosition = calculateAt(fillBuffersFrom, currentIndex);
                TimelinePosition endPosition = calculateAt(fillBuffersFrom, currentIndex + 1);

                executorService.submit(() -> {
                    if (latestPosition.isLessThan(endPosition)) {
                        byte[] audioFrame = playbackController.getAudioFrameAt(startPosition, AUDIOFRAME_NUMBER_PER_ELEMENT);
                        buffer.offer(new AudioData(startPosition, audioFrame));
                    }
                    inprogressItems.decrementAndGet();
                });
            }

        }
    }

    private TimelinePosition calculateAt(TimelinePosition fillBuffersFrom, int currentIndex) {
        return fillBuffersFrom.add(projectRepository.getFrameTime().multiply(BigDecimal.valueOf(currentIndex * AUDIOFRAME_NUMBER_PER_ELEMENT)));
    }

    private void streamAudio(TimelinePosition position) {
        dropFramesUntil(position);
        int availableBytesToWrite = audioStreamService.numberOfBytesThatCanBeWritten();

        if (availableBytesToWrite == 0) {
            return;
        }

        AudioData currentFrame = buffer.poll();

        if (currentFrame != null) {
            int writtenBytes = 0;

            byte[] tmpBytes = currentFrame.getBytesStartingFrom(position, availableBytesToWrite);
            audioStreamService.streamAudio(tmpBytes);
            availableBytesToWrite -= tmpBytes.length;
            writtenBytes += tmpBytes.length;

            AudioData previousFrame = currentFrame;

            while (availableBytesToWrite > 0 && (currentFrame = buffer.poll()) != null) {
                if (currentFrame.startPosition.isGreaterThan(previousFrame.startPosition)) {
                    tmpBytes = currentFrame.getBytes(availableBytesToWrite);
                    audioStreamService.streamAudio(tmpBytes);
                    availableBytesToWrite -= tmpBytes.length;
                    writtenBytes += tmpBytes.length;
                    previousFrame = currentFrame;
                } else {
                    currentFrame = null;
                }
            }

            if (availableBytesToWrite == 0 && currentFrame != null) { // Some data may be in the buffer still
                buffer.offerFirst(currentFrame);
            }

            if (writtenBytes > 0) {
                lastWrittenEndPosition = position.add(BigDecimal.valueOf(writtenBytes).divide(BYTES_PER_SECOND, 10, RoundingMode.HALF_UP));
            }
        } // else play some silence, alternatively we could block :)
    }

    private void dropFramesUntil(TimelinePosition position) {
        // TODO: user jumping back
        //        AudioData lastFrame = buffer.peekLast();
        //
        //        if (lastFrame != null && position.isGreaterThan(firstFrame.calculateEndPosition())) {
        //            buffer.clear(); // user jumped back on the timeline, need to reinitialize the buffers
        //        }

        AudioData data;
        while ((data = buffer.peek()) != null && !data.containsTime(position)) {
            buffer.poll();
        }
    }

    public void playbackStopped() {
        lastWrittenEndPosition = null;
        audioStreamService.clearBuffer();
    }

    static class AudioData {
        TimelinePosition startPosition;
        byte[] data;

        public AudioData(TimelinePosition position, byte[] data) {
            this.startPosition = position;
            this.data = data;
        }

        public TimelinePosition calculateEndPosition() {
            return new TimelinePosition(startPosition.getSeconds().add(BigDecimal.valueOf(data.length).divide(BYTES_PER_SECOND, 10, RoundingMode.HALF_UP)));
        }

        public boolean containsTime(TimelinePosition expectedPosition) {
            TimelinePosition endPosition = calculateEndPosition();
            return (expectedPosition.isLessOrEqualToThan(endPosition) && expectedPosition.isGreaterOrEqualToThan(startPosition));
        }

        public byte[] getBytes(int available) {
            int endPosition = Math.min(available, data.length);
            byte[] result = Arrays.copyOfRange(data, 0, endPosition);
            return result;
        }

        public byte[] getBytesStartingFrom(TimelinePosition position, int available) {
            int sampleStart = position.subtract(startPosition).getSeconds().multiply(BYTES_PER_SECOND).intValue();
            if ((sampleStart % BYTES_PER_SAMPLE) != 0) {
                sampleStart += ((BYTES_PER_SAMPLE - (sampleStart % BYTES_PER_SAMPLE)));
            }
            if (sampleStart <= 0) {
                return new byte[0];
            }
            int endPosition = Math.min(sampleStart + available, data.length);
            byte[] result = Arrays.copyOfRange(data, sampleStart, endPosition);
            return result;
        }

        @Override
        public String toString() {
            return "AudioData [startPosition=" + startPosition + "]";
        }

    }

}
