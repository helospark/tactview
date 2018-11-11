package com.helospark.tactview.ui.javafx.audio;

import javax.annotation.PostConstruct;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.logger.Slf4j;

@Component
public class AudioStreamService {
    private DataLine.Info dataLineInfo;
    private SourceDataLine sourceDataLine;

    AudioInputStream audioInputStream;
    static AudioInputStream ais;
    static AudioFormat format;

    @Slf4j
    private Logger logger;

    @PostConstruct
    public void init() {
        try {
            AudioFormat format = new AudioFormat(44100, 8, 2, true, false);
            dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(format);
            sourceDataLine.start();
        } catch (LineUnavailableException e) {
            logger.error("Unable to initialize audio subsystem", e);
        }
    }

    public void streamAudio(byte[] data) {
        int availableBytes = sourceDataLine.available();
        int bytesToWrite = Math.min(data.length, availableBytes);
        sourceDataLine.write(data, 0, bytesToWrite);
    }

}
