package com.helospark.tactview.ui.javafx.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.PlaybackFrameAccessor;

@Component
public class AudioStreamService {
    private DataLine.Info dataLineInfo;
    private SourceDataLine sourceDataLine = null;
    private boolean initFailed = false;

    @Slf4j
    private Logger logger;

    private int initializedFrameSize;

    public void streamAudio(byte[] data) {
        logger.debug("Streaming " + data.length + " at " + System.currentTimeMillis() + " with length "
                + (1000 * data.length / (PlaybackFrameAccessor.BYTES * PlaybackFrameAccessor.CHANNELS * PlaybackFrameAccessor.SAMPLE_RATE)));

        if (data.length > 0) {
            if ((sourceDataLine == null && !initFailed) || data.length > initializedFrameSize) { // TODO: reinit
                try {
                    logger.info("Initializing sourceDataLine with bufferSize={}", data.length);
                    AudioFormat format = new AudioFormat(PlaybackFrameAccessor.SAMPLE_RATE, PlaybackFrameAccessor.BYTES * 8, PlaybackFrameAccessor.CHANNELS, true, true);
                    dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
                    sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                    sourceDataLine.open(format, data.length);
                    sourceDataLine.start();
                } catch (LineUnavailableException | IllegalArgumentException e) {
                    logger.error("Unable to initialize sound, there will be no sound during playback", e);
                    initFailed = true;
                }
                initializedFrameSize = data.length;
            }

            if (!initFailed) {
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

    public int numberOfBytesThatCanBeWritten() {
        return sourceDataLine.available();
    }

    public void clearBuffer() {
        sourceDataLine.flush();
    }
}
