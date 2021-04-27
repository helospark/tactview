package com.helospark.tactview.ui.javafx.uicomponents.pattern;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.AudioRequest;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.ui.javafx.repository.SoundRmsRepository;
import com.helospark.tactview.ui.javafx.uicomponents.util.AudioRmsCalculator;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.scene.image.Image;

@Service
public class AudioImagePatternService {
    private static final int NUMBER_OF_PIXELS_FOR_SAMPLE = 1;
    private static final int RECTANGLE_HEIGHT = 50;

    private final ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter;
    private final ProjectRepository projectRepository;
    private final SoundRmsRepository soundRmsRepository;
    private final AudioRmsCalculator audioRmsCalculator;

    public AudioImagePatternService(ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter, ProjectRepository projectRepository, SoundRmsRepository soundRmsRepository,
            AudioRmsCalculator audioRmsCalculator) {
        this.byteBufferToJavaFxImageConverter = byteBufferToJavaFxImageConverter;
        this.projectRepository = projectRepository;
        this.soundRmsRepository = soundRmsRepository;
        this.audioRmsCalculator = audioRmsCalculator;
    }

    public Image createAudioImagePattern(AudibleTimelineClip audibleTimelineClip, int width, double visibleStartPosition, double visibleEndPosition) {
        int scaledFrameWidth = width;
        int scaledFrameHeight = RECTANGLE_HEIGHT;
        int numberOfChannels = projectRepository.getNumberOfChannels();
        int channelHeight = scaledFrameHeight / numberOfChannels;
        double maxRmsToDisplayUiWith = soundRmsRepository.getMaxRms();

        List<MutableInteger> lastPointPerChannel = new ArrayList<>();
        for (int i = 0; i < numberOfChannels; ++i) {
            lastPointPerChannel.add(new MutableInteger((i + 1) * channelHeight));
        }

        BigDecimal lengthInSeconds = BigDecimal.valueOf(visibleEndPosition - visibleStartPosition);
        BigDecimal secondsPerPixel = lengthInSeconds.divide(BigDecimal.valueOf(width), 10, RoundingMode.HALF_UP);

        int numberOfSamplesToCollect = lengthInSeconds
                .divide(secondsPerPixel, 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(NUMBER_OF_PIXELS_FOR_SAMPLE), 10, RoundingMode.HALF_UP)
                .intValue();
        BigDecimal timeJump = lengthInSeconds.divide(BigDecimal.valueOf(numberOfSamplesToCollect), 10, RoundingMode.HALF_UP);

        BufferedImage result = new BufferedImage(scaledFrameWidth, scaledFrameHeight, TYPE_INT_RGB);
        Graphics graphics = result.getGraphics();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, scaledFrameWidth, scaledFrameHeight);
        graphics.setColor(Color.GRAY);

        for (int i = 0; i < numberOfChannels; ++i) {
            int y = ((i + 1) * channelHeight) + 1;
            graphics.drawLine(0, y, width, y);
        }
        graphics.setColor(new Color(0, 255, 0, 200));
        double currentMaxRms = 0.0;

        for (int i = 0; i < numberOfSamplesToCollect; ++i) {
            AudioRequest frameRequest = AudioRequest.builder()
                    .withApplyEffects(false)
                    .withPosition(audibleTimelineClip.getInterval().getStartPosition().add(BigDecimal.valueOf(visibleStartPosition)).add(timeJump.multiply(BigDecimal.valueOf(i))))
                    .withLength(TimelineLength.ofMillis(1))
                    .withSampleRate(projectRepository.getSampleRate())
                    .withBytesPerSample(projectRepository.getBytesPerSample())
                    .withNumberOfChannels(projectRepository.getNumberOfChannels())
                    .build();
            AudioFrameResult frame = audibleTimelineClip.requestAudioFrame(frameRequest);

            for (int j = 0; j < numberOfChannels; ++j) {
                double rms = audioRmsCalculator.calculateRms(frame, j);
                if (rms > currentMaxRms) {
                    currentMaxRms = rms;
                }
                int point = soundHeight(rms, channelHeight, maxRmsToDisplayUiWith);

                MutableInteger lastPoint = lastPointPerChannel.get(j);
                int newPointY = ((j + 1) * channelHeight) - point - 1;
                graphics.drawLine((i + 1) * NUMBER_OF_PIXELS_FOR_SAMPLE, ((j + 1) * channelHeight), (i + 1) * NUMBER_OF_PIXELS_FOR_SAMPLE, newPointY);
                lastPoint.y = newPointY;
            }
            frame.getChannels()
                    .stream()
                    .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a));
        }
        // tight coupling would be good to avoid here, but calculating rms for full clip is expensive to do twice
        // maybe some cache magic could work?!
        soundRmsRepository.setRmsForClip(audibleTimelineClip.getId(), currentMaxRms);

        return byteBufferToJavaFxImageConverter.convertToJavafxImage(result);
    }

    private int soundHeight(double rms, int channelHeight, double maxRms) {
        return Math.min((int) ((rms / maxRms) * channelHeight), channelHeight);
    }

    private static class MutableInteger {
        public int y;

        public MutableInteger(int y) {
            this.y = y;
        }

    }

}
