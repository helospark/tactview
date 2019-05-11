package com.helospark.tactview.ui.javafx.uicomponents.pattern;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.scene.image.Image;

@Service
public class AudioImagePatternService {
    private static final int NUMBER_OF_PIXELS_FOR_SAMPLE = 1;
    private static final int SAMPLE_MAX_VALUE = 140; // TODO: this cake is a lie, needs some dynamic range or something
    private static final int RECTANGLE_HEIGHT = 50;

    private ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter;
    private ProjectRepository projectRepository;

    public AudioImagePatternService(ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter, ProjectRepository projectRepository) {
        this.byteBufferToJavaFxImageConverter = byteBufferToJavaFxImageConverter;
        this.projectRepository = projectRepository;
    }

    public Image createAudioImagePattern(AudibleTimelineClip audibleTimelineClip, int width) {
        return null;
        //        int scaledFrameWidth = width;
        //        int scaledFrameHeight = RECTANGLE_HEIGHT;
        //        int numberOfChannels = audibleTimelineClip.getMediaMetadata().getChannels();
        //        int channelHeight = scaledFrameHeight / numberOfChannels;
        //
        //        List<MutableInteger> lastPointPerChannel = new ArrayList<>();
        //        for (int i = 0; i < numberOfChannels; ++i) {
        //            lastPointPerChannel.add(new MutableInteger((i + 1) * channelHeight));
        //        }
        //
        //        TimelineInterval interval = audibleTimelineClip.getInterval();
        //        BigDecimal lengthInSeconds = interval.getLength().getSeconds();
        //        BigDecimal secondsPerPixel = lengthInSeconds.divide(BigDecimal.valueOf(width), 2, RoundingMode.HALF_UP);
        //
        //        int numberOfSamplesToCollect = lengthInSeconds
        //                .divide(secondsPerPixel, 2, RoundingMode.HALF_UP)
        //                .divide(BigDecimal.valueOf(NUMBER_OF_PIXELS_FOR_SAMPLE), 2, RoundingMode.HALF_UP)
        //                .intValue();
        //        BigDecimal timeJump = lengthInSeconds.divide(BigDecimal.valueOf(numberOfSamplesToCollect), 2, RoundingMode.HALF_UP);
        //
        //        BufferedImage result = new BufferedImage(scaledFrameWidth, scaledFrameHeight, TYPE_INT_RGB);
        //        Graphics graphics = result.getGraphics();
        //
        //        graphics.setColor(Color.BLACK);
        //        graphics.fillRect(0, 0, scaledFrameWidth, scaledFrameHeight);
        //        graphics.setColor(Color.GRAY);
        //
        //        for (int i = 0; i < numberOfChannels; ++i) {
        //            int y = ((i + 1) * channelHeight) + 1;
        //            graphics.drawLine(0, y, width, y);
        //        }
        //        graphics.setColor(Color.GREEN);
        //
        //        for (int i = 0; i < numberOfSamplesToCollect; ++i) {
        //            AudioRequest frameRequest = AudioRequest.builder()
        //                    .withApplyEffects(false)
        //                    .withPosition(audibleTimelineClip.getInterval().getStartPosition().add(timeJump.multiply(BigDecimal.valueOf(i))))
        //                    .withLength(TimelineLength.ofMillis(1))
        //                    .withSampleRate(projectRepository.getSampleRate())
        //                    .withBytesPerSample(projectRepository.getBytesPerSample())
        //                    .build();
        //            AudioFrameResult frame = audibleTimelineClip.requestAudioFrame(frameRequest);
        //
        //            for (int j = 0; j < numberOfChannels; ++j) {
        //                int point = valumeOnSample(frame, j, channelHeight);
        //
        //                MutableInteger lastPoint = lastPointPerChannel.get(j);
        //                int newPointY = ((j + 1) * channelHeight) - point - 1;
        //                graphics.drawLine(i * NUMBER_OF_PIXELS_FOR_SAMPLE, lastPoint.y, (i + 1) * NUMBER_OF_PIXELS_FOR_SAMPLE, newPointY);
        //                lastPoint.y = newPointY;
        //            }
        //            frame.getChannels()
        //                    .stream()
        //                    .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a));
        //        }
        //
        //        return byteBufferToJavaFxImageConverter.convertToJavafxImage(result);
    }

    private int valumeOnSample(AudioFrameResult frame, int channelNumber, int channelHeight) {
        if (frame.getNumberSamples() == 0) {
            return 0;
        }
        int result = 0;
        for (int i = 0; i < frame.getNumberSamples(); ++i) {
            double sample = frame.getRescaledSample(channelNumber, 1, frame.getSamplePerSecond(), i);
            result += (sample * sample);
        }
        double rms = Math.sqrt((double) result / (frame.getNumberSamples()));
        //System.out.println(rms);
        return Math.min((int) ((rms / SAMPLE_MAX_VALUE) * channelHeight), channelHeight);

    }

    private static class MutableInteger {
        public int y;

        public MutableInteger(int y) {
            this.y = y;
        }

    }

}
