package com.helospark.tactview.ui.javafx.uicomponents.pattern;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Map;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.timeline.render.FrameExtender;
import com.helospark.tactview.core.timeline.render.FrameExtender.FrameExtendRequest;
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
    private FrameExtender frameExtender;
    private TimelineManagerRenderService timelineManagerRenderService;
    private TimelineManagerAccessor timelineManagerAccessor;

    public TimelineImagePatternService(UiProjectRepository uiProjectRepository, ByteBufferToImageConverter byteBufferToImageConverter,
            ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter, FrameExtender frameExtender,
            TimelineManagerRenderService timelineManagerRenderService, TimelineManagerAccessor timelineManagerAccessor) {
        this.uiProjectRepository = uiProjectRepository;
        this.byteBufferToImageConverter = byteBufferToImageConverter;
        this.byteBufferToJavaFxImageConverter = byteBufferToJavaFxImageConverter;
        this.frameExtender = frameExtender;
        this.timelineManagerRenderService = timelineManagerRenderService;
        this.timelineManagerAccessor = timelineManagerAccessor;
    }

    public Image createTimelinePattern(VisualTimelineClip videoClip, int expectedWidth, double visibleStartPosition, double visibleEndPosition) {
        VisualMediaMetadata metadata = videoClip.getMediaMetadata();
        int scaledFrameWidth = (int) ((double) metadata.getWidth() / metadata.getHeight() * PREVIEW_HEIGHT);
        int scaledFrameHeight = PREVIEW_HEIGHT;

        int numberOfFrames = (int) Math.ceil((double) (expectedWidth + BLACK_FILM_TAPE_LINE_WIDTH) / scaledFrameWidth);

        double timejump = (visibleEndPosition - visibleStartPosition) / numberOfFrames;

        BufferedImage result = new BufferedImage(expectedWidth, RECTANGLE_HEIGHT, TYPE_INT_RGB);
        Graphics graphics = result.getGraphics();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, expectedWidth, FILM_TAPE_SIZE);

        boolean isDynamicallyGenerated = videoClip instanceof ProceduralVisualClip;
        int index = 0;
        for (double seconds = visibleStartPosition; seconds < visibleEndPosition; seconds += timejump, ++index) {
            TimelinePosition position = TimelinePosition.ofSeconds(seconds);
            int width = isDynamicallyGenerated ? uiProjectRepository.getPreviewWidth() : scaledFrameWidth;
            int height = isDynamicallyGenerated ? uiProjectRepository.getPreviewHeight() : scaledFrameHeight;
            double scale = (double) width / metadata.getWidth();

            GetFrameRequest frameRequest = GetFrameRequest.builder()
                    .withApplyEffects(false)
                    .withUseApproximatePosition(true)
                    .withExpectedWidth(width)
                    .withExpectedHeight(height)
                    .withRelativePosition(position)
                    .withScale(scale)
                    .withEvaluationContext(createEvaluationContext(position, width, height, scale))
                    .build();
            ReadOnlyClipImage frame = videoClip.getFrame(frameRequest);

            if (isDynamicallyGenerated) {
                FrameExtendRequest extendFrameRequest = FrameExtendRequest.builder()
                        .withClip(videoClip)
                        .withFrameResult(frame)
                        .withPreviewWidth(uiProjectRepository.getPreviewWidth())
                        .withPreviewHeight(uiProjectRepository.getPreviewHeight())
                        .withScale(uiProjectRepository.getScaleFactor())
                        .withTimelinePosition(position.add(videoClip.getInterval().getStartPosition()))
                        .build();

                ClipImage expandedFrame = frameExtender.expandFrame(extendFrameRequest);

                BufferedImage bf = byteBufferToImageConverter.byteBufferToBufferedImage(expandedFrame.getBuffer(), expandedFrame.getWidth(), expandedFrame.getHeight());
                java.awt.Image img = bf.getScaledInstance(scaledFrameWidth, scaledFrameHeight, BufferedImage.SCALE_SMOOTH);
                GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frame.getBuffer());
                GlobalMemoryManagerAccessor.memoryManager.returnBuffer(expandedFrame.getBuffer());
                graphics.drawImage(img, index * (scaledFrameWidth + BLACK_FILM_TAPE_LINE_WIDTH) + BLACK_FILM_TAPE_LINE_WIDTH, FILM_TAPE_SIZE, null);
            } else {
                BufferedImage bf = byteBufferToImageConverter.byteBufferToBufferedImage(frame.getBuffer(), frame.getWidth(), frame.getHeight());
                graphics.drawImage(bf, index * (scaledFrameWidth + BLACK_FILM_TAPE_LINE_WIDTH) + BLACK_FILM_TAPE_LINE_WIDTH, FILM_TAPE_SIZE, null);
                GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frame.getBuffer());
            }
        }

        dragFilmEffect(expectedWidth, graphics);

        return byteBufferToJavaFxImageConverter.convertToJavafxImage(result);
    }

    private EvaluationContext createEvaluationContext(TimelinePosition position, int width, int height, double scale) {
        return timelineManagerRenderService.createEvaluationContext(timelineManagerAccessor.findIntersectingClipsData(position), createGlobalsMap(position, width, height, scale));
    }

    private Map<String, Object> createGlobalsMap(TimelinePosition position, int width, int height, double scale) {
        return Map.of(
                "time", position.getSeconds().doubleValue(),
                "width", width,
                "height", height,
                "scale", scale);
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
