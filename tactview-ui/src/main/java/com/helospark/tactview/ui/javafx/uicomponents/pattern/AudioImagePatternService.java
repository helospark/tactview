package com.helospark.tactview.ui.javafx.uicomponents.pattern;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.preference.PreferenceValue;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.AudioRequest;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.scene.image.Image;

@Service
public class AudioImagePatternService {
    private static final int NUMBER_OF_PIXELS_FOR_SAMPLE = 1;
    public static final int DEFAULT_HEIGHT = 50;

    private final ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter;
    private final ProjectRepository projectRepository;
    private final TimelineManagerRenderService timelineManagerRenderService;
    private final TimelineManagerAccessor timelineManagerAccessor;
    private boolean renderAvgLine = true;

    public AudioImagePatternService(ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter, ProjectRepository projectRepository, TimelineManagerRenderService timelineManagerRenderService,
            TimelineManagerAccessor timelineManagerAccessor) {
        this.byteBufferToJavaFxImageConverter = byteBufferToJavaFxImageConverter;
        this.projectRepository = projectRepository;
        this.timelineManagerRenderService = timelineManagerRenderService;
        this.timelineManagerAccessor = timelineManagerAccessor;
    }

    @PreferenceValue(name = "Render avg line for audio clip", defaultValue = "true", group = "Performance")
    public void setImageClipLength(boolean renderAvgLine) {
        this.renderAvgLine = renderAvgLine;
    }

    public Image createAudioImagePattern(AudibleTimelineClip audibleTimelineClip, int width, int height, double visibleStartPosition, double visibleEndPosition) {
        int scaledFrameWidth = width;
        int scaledFrameHeight = height;
        int numberOfChannels = projectRepository.getNumberOfChannels();
        int channelHeight = scaledFrameHeight / numberOfChannels;

        BigDecimal lengthInSeconds = BigDecimal.valueOf(visibleEndPosition - visibleStartPosition);
        BigDecimal secondsPerPixel = lengthInSeconds.divide(BigDecimal.valueOf(width), 10, RoundingMode.HALF_UP);

        int numberOfSamplesToCollect = lengthInSeconds
                .divide(secondsPerPixel, 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(NUMBER_OF_PIXELS_FOR_SAMPLE), 10, RoundingMode.HALF_UP)
                .intValue();
        BigDecimal timeJump = lengthInSeconds.divide(BigDecimal.valueOf(numberOfSamplesToCollect), 10, RoundingMode.HALF_UP);

        BufferedImage result = new BufferedImage(scaledFrameWidth, scaledFrameHeight, TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) result.getGraphics();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, scaledFrameWidth, scaledFrameHeight);
        graphics.setColor(Color.DARK_GRAY);

        for (int i = 0; i < numberOfChannels; ++i) {
            int y = ((i + 1) * channelHeight) + 1;
            graphics.drawLine(0, y, width, y);
        }

        for (int i = 0; i < numberOfSamplesToCollect; ++i) {
            TimelinePosition position = audibleTimelineClip.getInterval().getStartPosition().add(BigDecimal.valueOf(visibleStartPosition)).add(timeJump.multiply(BigDecimal.valueOf(i)));
            AudioRequest frameRequest = AudioRequest.builder()
                    .withApplyEffects(false)
                    .withPosition(position)
                    .withLength(new TimelineLength(timeJump))
                    .withSampleRate(projectRepository.getSampleRate())
                    .withBytesPerSample(projectRepository.getBytesPerSample())
                    .withNumberOfChannels(projectRepository.getNumberOfChannels())
                    .withEvaluationContext(createEvaluationContext(position))
                    .build();
            AudioFrameResult frame = audibleTimelineClip.requestAudioFrame(frameRequest);

            for (int j = 0; j < numberOfChannels; ++j) {
                MutableSampleResult sampleData = getSampleResult(frame, j);

                double maxHeight = channelHeight / 2.0;
                double centerPoint = (j + 1) * channelHeight - maxHeight;
                int startPointY = (int) (centerPoint + Math.abs(sampleData.min) * maxHeight);
                int endPointY = (int) (centerPoint - Math.abs(sampleData.max) * maxHeight);

                graphics.setColor(new Color(0, 255, 0, 200));
                graphics.setStroke(new BasicStroke(NUMBER_OF_PIXELS_FOR_SAMPLE));
                graphics.drawLine((i + 1) * NUMBER_OF_PIXELS_FOR_SAMPLE, startPointY, (i + 1) * NUMBER_OF_PIXELS_FOR_SAMPLE, endPointY);

                if (renderAvgLine) {
                    graphics.setColor(new Color(0, 100, 0, 200));

                    int avgStartPointY = (int) (centerPoint + Math.abs(sampleData.negativeAvg) * maxHeight);
                    int avgEndPointY = (int) (centerPoint - Math.abs(sampleData.positiveAvg) * maxHeight);

                    graphics.drawLine((i + 1) * NUMBER_OF_PIXELS_FOR_SAMPLE, avgStartPointY, (i + 1) * NUMBER_OF_PIXELS_FOR_SAMPLE, avgEndPointY);
                }
            }
            frame.getChannels()
                    .stream()
                    .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a));
        }

        return byteBufferToJavaFxImageConverter.convertToJavafxImage(result);
    }

    private EvaluationContext createEvaluationContext(TimelinePosition position) {
        return timelineManagerRenderService.createEvaluationContext(timelineManagerAccessor.findIntersectingClipsData(position), createGlobalsMap(position));
    }

    private Map<String, Object> createGlobalsMap(TimelinePosition position) {
        return Map.of(
                "time", position.getSeconds().doubleValue());
    }

    private MutableSampleResult getSampleResult(AudioFrameResult frame, int channelNumber) {
        double min = 0;
        double max = 0;

        int positiveSumCount = 0;
        double positiveSum = 0;

        int negativeSumCount = 0;
        double negativeSum = 0;

        for (int i = 0; i < frame.getNumberSamples(); ++i) {
            double sample = frame.getNormalizedSampleAt(channelNumber, i);
            if (sample > max) {
                max = sample;
            }
            if (sample < min) {
                min = sample;
            }
            if (sample >= 0.0) {
                positiveSum += sample;
                ++positiveSumCount;
            }
            if (sample < 0.0) {
                negativeSum += -1 * sample;
                ++negativeSumCount;
            }
        }

        double negativeAvg = negativeSumCount != 0 ? negativeSum / negativeSumCount : 0.0;
        double positiveAvg = positiveSumCount != 0 ? positiveSum / positiveSumCount : 0.0;

        return new MutableSampleResult(max, min, negativeAvg, positiveAvg);
    }

    static class MutableSampleResult {
        double max;
        double min;
        double negativeAvg;
        double positiveAvg;

        public MutableSampleResult(double max, double min, double negativeAvg, double positiveAvg) {
            this.max = max;
            this.min = min;
            this.negativeAvg = negativeAvg;
            this.positiveAvg = positiveAvg;
        }

    }

}
