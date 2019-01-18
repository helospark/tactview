package com.helospark.tactview.ui.javafx;

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.core.util.ThreadSleep;
import com.helospark.tactview.ui.javafx.audio.AudioStreamService;

@Component
public class AudioUpdaterService {
    private static final int NUMBER_OF_FRAMES_TO_KEEP_IN_MEMORY = 30;

    private UiTimelineManager uiTimelineManager;
    private PlaybackController playbackController;
    private AudioStreamService audioStreamService;
    private ProjectRepository projectRepository;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private Thread soundUpdaterThread;
    private Runnable audioThreadUpdater;

    private volatile AudioData[] buffer = new AudioData[2];
    private volatile int activeBuffer = 0;
    private volatile TimelinePosition expectedPosition = null;
    private volatile TimelinePosition lastWrittenEndPosition = null;
    private volatile TimelinePosition lastReadPosition = null;
    private volatile boolean audioPlaybackProcessing = false;
    private volatile Future<Object> threadUpdaterFuture = null;

    public AudioUpdaterService(UiTimelineManager uiTimelineManager, PlaybackController playbackController, AudioStreamService audioStreamService, ProjectRepository projectRepository) {
        this.uiTimelineManager = uiTimelineManager;
        this.playbackController = playbackController;
        this.audioStreamService = audioStreamService;
        this.projectRepository = projectRepository;
    }

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        audioThreadUpdater = () -> {
            while (audioPlaybackProcessing) {
                try {
                    if (lastWrittenEndPosition == null || (lastWrittenEndPosition.subtract(expectedPosition).multiply(BigDecimal.valueOf(1000)).isLessThan(5))) {
                        int available = audioStreamService.numberOfBytesThatCanBeWritten();

                        if (buffer[activeBuffer] == null) {
                            threadUpdaterFuture.get();
                            if (buffer[activeBuffer] == null) {
                                readDataToBufferAtPosition(expectedPosition, activeBuffer);
                            }
                        }

                        byte[] bytes = buffer[activeBuffer].getBytes(available); /** channels**/

                        audioStreamService.streamAudio(bytes);

                        lastWrittenEndPosition = calculateEndPosition(buffer[activeBuffer].startPosition, bytes.length);

                        if (!buffer[activeBuffer].hasMoreBytes()) {
                            buffer[activeBuffer] = null;
                            int bufferToUpdate = activeBuffer;
                            threadUpdaterFuture = (Future<Object>) executorService.submit(() -> {
                                TimelinePosition nextPosition = calculateNextTime(lastReadPosition);
                                readDataToBufferAtPosition(nextPosition, bufferToUpdate);
                            });
                            activeBuffer = (activeBuffer + 1) % 2;
                        }
                    }

                    int millisecondsToSleep = MathUtil.clamp(lastWrittenEndPosition.subtract(expectedPosition).getSeconds().multiply(BigDecimal.valueOf(1000)).intValue(), 0, 2000);
                    ThreadSleep.sleep(millisecondsToSleep);
                } catch (Exception e) {
                    e.printStackTrace();
                    ThreadSleep.sleep(100);
                }
            }
        };

    }

    private TimelinePosition calculateNextTime(TimelinePosition currentPosition) {
        return currentPosition.add(projectRepository.getFrameTime().multiply(BigDecimal.valueOf(NUMBER_OF_FRAMES_TO_KEEP_IN_MEMORY)));
    }

    private void readDataToBufferAtPosition(TimelinePosition position, int bufferToUpdate) {
        TimelinePosition newPosition = position;
        byte[] data = playbackController.getAudioFrameAt(newPosition, NUMBER_OF_FRAMES_TO_KEEP_IN_MEMORY);
        lastReadPosition = newPosition;
        AudioData audioData = new AudioData(newPosition, data);
        buffer[bufferToUpdate] = audioData;
        System.out.println("Updated buffer at " + position + " " + bufferToUpdate);
    }

    private TimelinePosition calculateEndPosition(TimelinePosition startPosition, int bytes) {
        // 2 bytes, 44100 samples, 1 channels
        BigDecimal time = BigDecimal.valueOf(bytes).divide(BigDecimal.valueOf(44100 * 2 * 1), 20, HALF_UP);
        System.out.println("Updating time with " + time + " " + buffer[activeBuffer].startPosition);
        return startPosition.add(time);
    }

    public void updateAtPosition(TimelinePosition position) {
        if (uiTimelineManager.isPlaybackInProgress()) {
            this.expectedPosition = position;

            if (!audioPlaybackProcessing) {
                readDataToBufferAtPosition(position, 0);
                activeBuffer = 0;
                threadUpdaterFuture = executorService.submit(() -> calculateNextTime(position), 1);
                audioPlaybackProcessing = true;
                soundUpdaterThread = new Thread(audioThreadUpdater);
                soundUpdaterThread.start();
            }
        }
    }

    public void playbackStopped() {
        audioPlaybackProcessing = false;
        audioStreamService.clearBuffer();
        lastWrittenEndPosition = null;
        lastReadPosition = null;
        expectedPosition = null;
        buffer[0] = null;
        buffer[1] = null;
        soundUpdaterThread = null;
    }

    static class AudioData {
        TimelinePosition startPosition;
        byte[] data;
        int index;

        public AudioData(TimelinePosition position, byte[] data) {
            this.startPosition = position;
            this.data = data;
            this.index = 0;
        }

        public boolean hasMoreBytes() {
            return index < data.length;
        }

        public byte[] getBytes(int available) {
            int endPosition = Math.min(index + available, data.length);
            byte[] result = Arrays.copyOfRange(data, index, endPosition);
            index = endPosition;
            return result;
        }
    }

}
