package com.helospark.tactview.ui.javafx.audio;

import javax.annotation.PostConstruct;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.PlaybackController;

@Component
public class AudioStreamService {
    private DataLine.Info dataLineInfo;
    private SourceDataLine sourceDataLine;

    @Slf4j
    private Logger logger;

    @PostConstruct
    public void init() {
        try {
            AudioFormat format = new AudioFormat(PlaybackController.FREQUENCY, PlaybackController.BYTES * 8, PlaybackController.CHANNELS, true, true);
            dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(format);
            sourceDataLine.start();
        } catch (LineUnavailableException e) {
            logger.error("Unable to initialize audio subsystem", e);
        }
    }

    public void streamAudio(byte[] data) {
        System.out.println("Streaming " + data.length);
        if (data.length > 0) {
            //            System.out.println("Sending this music: ");
            //            for (int i = 0; i < data.length && i < 500; ++i) {
            //                System.out.print(((int) data[i]) + " ");
            //            }
            //            System.out.println();

            int availableBytes = sourceDataLine.available();
            int bytesToWrite = Math.min(data.length, availableBytes);
            sourceDataLine.write(data, 0, bytesToWrite - (bytesToWrite % 2));
        }
    }

    public int numberOfBytesThatCanBeWritten() {
        return sourceDataLine.available();
    }

    public void clearBuffer() {
        sourceDataLine.flush();
    }
}
