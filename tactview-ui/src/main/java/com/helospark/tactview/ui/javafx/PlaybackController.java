package com.helospark.tactview.ui.javafx;

import java.nio.ByteBuffer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.scene.image.Image;

@Component
public class PlaybackController {
    private TimelineManager timelineManager;
    private UiProjectRepository uiProjectRepository;
    private ByteBufferToJavaFxImageConverter byteBufferToImageConverter;

    public PlaybackController(TimelineManager timelineManager, UiProjectRepository uiProjectRepository,
            ByteBufferToJavaFxImageConverter byteBufferToImageConverter) {
        this.timelineManager = timelineManager;
        this.uiProjectRepository = uiProjectRepository;
        this.byteBufferToImageConverter = byteBufferToImageConverter;
    }

    public Image getFrameAt(TimelinePosition position) {
        Integer width = uiProjectRepository.getPreviewWidth();
        Integer height = uiProjectRepository.getPreviewHeight();
        TimelineManagerFramesRequest request = TimelineManagerFramesRequest.builder()
                .withFrameBufferSize(1)
                .withPosition(position)
                .withScale(uiProjectRepository.getScaleFactor())
                .withPreviewWidth(width)
                .withPreviewHeight(height)
                .build();
        ByteBuffer frame = timelineManager.getFrames(request);
        Image javafxImage = byteBufferToImageConverter.convertToJavafxImage(frame, width, height);
        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frame);
        return javafxImage;
    }
}
