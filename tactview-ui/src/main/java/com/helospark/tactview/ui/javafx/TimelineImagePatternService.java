package com.helospark.tactview.ui.javafx;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.util.ByteBufferToImageConverter;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.scene.image.Image;

@Service
public class TimelineImagePatternService {
    private static final int RECTANGLE_HEIGHT = 50;
    private static final int FILM_TAPE_SIZE = 5;
    private static final int PREVIEW_HEIGHT = RECTANGLE_HEIGHT - 2 * FILM_TAPE_SIZE;
    private static final int FILM_TAPE_HOLE_DISTANCE = 10;
    private static final int BLACK_FILM_TAPE_LINE_WIDTH = 1;

    private UiProjectRepository uiProjectRepository;
    private ByteBufferToImageConverter byteBufferToImageConverter;
    private ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter;

    public TimelineImagePatternService(UiProjectRepository uiProjectRepository, ByteBufferToImageConverter byteBufferToImageConverter,
            ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter) {
        this.uiProjectRepository = uiProjectRepository;
        this.byteBufferToImageConverter = byteBufferToImageConverter;
        this.byteBufferToJavaFxImageConverter = byteBufferToJavaFxImageConverter;
    }

    public Image createTimelinePattern(VisualTimelineClip videoClip, int timelineWidth) {
        return doIt(videoClip, timelineWidth);
    }

    private Image doIt(VisualTimelineClip videoClip, int timelineWidth) {
        VisualMediaMetadata metadata = videoClip.getMediaMetadata();
        int scaledFrameWidth = (int) ((double) metadata.getWidth() / metadata.getHeight() * PREVIEW_HEIGHT);
        int scaledFrameHeight = PREVIEW_HEIGHT;

        TimelineInterval interval = videoClip.getInterval();

        int numberOfFrames = (int) Math.ceil((double) (timelineWidth + BLACK_FILM_TAPE_LINE_WIDTH) / scaledFrameWidth);

        BigDecimal timejump = interval.getLength()
                .getSeconds()
                .divide(BigDecimal.valueOf(numberOfFrames), 2, RoundingMode.HALF_UP);

        BufferedImage result = new BufferedImage(timelineWidth, RECTANGLE_HEIGHT, TYPE_INT_RGB);
        Graphics graphics = result.getGraphics();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, timelineWidth, FILM_TAPE_SIZE);

        for (int i = 0; i < numberOfFrames; ++i) {
            GetFrameRequest frameRequest = GetFrameRequest.builder()
                    .withApplyEffects(false)
                    .withExpectedWidth(uiProjectRepository.getPreviewWidth())
                    .withExpectedHeight(uiProjectRepository.getPreviewHeight())
                    .withRelativePosition(new TimelinePosition(timejump.multiply(BigDecimal.valueOf(i))))
                    .withScale(uiProjectRepository.getScaleFactor())
                    .build();
            ClipFrameResult frame = videoClip.getFrame(frameRequest);
            BufferedImage bf = byteBufferToImageConverter.byteBufferToBufferedImage(frame.getBuffer(), frame.getWidth(), frame.getHeight());
            java.awt.Image img = bf.getScaledInstance(scaledFrameWidth, scaledFrameHeight, BufferedImage.SCALE_SMOOTH);
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frame.getBuffer());
            graphics.drawImage(img, i * (scaledFrameWidth + BLACK_FILM_TAPE_LINE_WIDTH) + BLACK_FILM_TAPE_LINE_WIDTH, FILM_TAPE_SIZE, null);
        }

        dragFilmEffect(timelineWidth, graphics);

        return byteBufferToJavaFxImageConverter.convertToJavafxImage(result);
    }

    private void dragFilmEffect(int timelineWidth, Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, timelineWidth, FILM_TAPE_SIZE);
        graphics.fillRect(0, RECTANGLE_HEIGHT - FILM_TAPE_SIZE, timelineWidth, FILM_TAPE_SIZE);

        graphics.setColor(Color.WHITE);
        for (int i = 0; i < timelineWidth; i += FILM_TAPE_HOLE_DISTANCE) {
            graphics.fillRect(i, 2, 3, 2);
            graphics.fillRect(i, RECTANGLE_HEIGHT - 4, 3, 2);
        }
    }

}
