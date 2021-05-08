package com.helospark.tactview.ui.javafx.audio;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.repository.ProjectSizeChangedMessage;
import com.helospark.tactview.core.repository.ProjectSizeChangedMessage.ProjectSizeChangeType;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.PlaybackFrameAccessor;

@Component
public class AudioStreamService {
    private SourceDataLine sourceDataLine = null;

    private MessagingService messagingService;
    private ProjectRepository projectRepository;

    @Slf4j
    private Logger logger;

    private volatile boolean isInitialized = false;

    public AudioStreamService(MessagingService messagingService, ProjectRepository projectRepository) {
        this.messagingService = messagingService;
        this.projectRepository = projectRepository;
    }

    @PostConstruct
    public void init() {
        messagingService.register(ProjectSizeChangedMessage.class, message -> {
            if (message.getType().equals(ProjectSizeChangeType.AUDIO) || message.getType().equals(ProjectSizeChangeType.CLEARED)) {
                int length = projectRepository.getFrameTime().multiply(BigDecimal.valueOf(PlaybackFrameAccessor.SAMPLE_RATE)).intValue() * PlaybackFrameAccessor.BYTES * PlaybackFrameAccessor.CHANNELS;
                initializeProjectRepository(length);
            }
        });
    }

    public void streamAudio(byte[] data) {
        logger.debug("Streaming " + data.length + " at " + System.currentTimeMillis() + " with length "
                + (1000 * data.length / (PlaybackFrameAccessor.BYTES * PlaybackFrameAccessor.CHANNELS * PlaybackFrameAccessor.SAMPLE_RATE)));

        if (data.length > 0) {
            if (isInitialized) {
                logger.debug("There is still " + (sourceDataLine.getBufferSize() - sourceDataLine.available()) + " bytes in the buffer at " + System.currentTimeMillis());
                int bytesWritten = sourceDataLine.write(data, 0, data.length);
                logger.debug("Bytes written: " + bytesWritten + " " + data.length + " at " + System.currentTimeMillis());
            } else {
                try {
                    int lengthInMs = (data.length * 1000) / (PlaybackFrameAccessor.CHANNELS * PlaybackFrameAccessor.BYTES * PlaybackFrameAccessor.SAMPLE_RATE);
                    Thread.sleep(lengthInMs); // Simulate sound played, because frame speed is driven by blocking sound write
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initializeProjectRepository(int size) {
        try {
            isInitialized = false;
            logger.info("Initializing sourceDataLine with bufferSize={}", size);

            AudioFormat defaultAudioFormat = new AudioFormat(PlaybackFrameAccessor.SAMPLE_RATE, PlaybackFrameAccessor.BYTES * 8, PlaybackFrameAccessor.CHANNELS, true, true);
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, defaultAudioFormat);
            logger.debug("Is audio line supported={}", AudioSystem.isLineSupported(dataLineInfo));

            // TODO: iterate over formats to find one that is matching in case isLineSupported returns false

            if (sourceDataLine != null) {
                sourceDataLine.close();
                sourceDataLine = null;
            }

            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(defaultAudioFormat, size);
            sourceDataLine.start();

            // Bug(?) in openJDK. Without this later sourceDataLine.write hangs indefinitely sometimes...
            // See https://gist.github.com/helospark/9406f093cc39fe8c4ccf1ea61951e4ee that shows it on some computers (but not on others)
            sourceDataLine.write(new byte[4], 0, 4);

            isInitialized = true;
            logger.info("SourceDataLine is initialized");
        } catch (LineUnavailableException | IllegalArgumentException e) {
            logger.error("Unable to initialize sound, there will be no sound during playback", e);
        }
    }

    public int numberOfBytesThatCanBeWritten() {
        return sourceDataLine.available();
    }

    public void clearBuffer() {
        sourceDataLine.flush();
    }
}
