package com.helospark.tactview.ui.javafx;

import java.nio.ByteBuffer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;

import javafx.scene.image.Image;

@Component
public class PlaybackController {
    private TimelineManager timelineManager;
    private UiProjectRepository uiProjectRepository;

    public PlaybackController(TimelineManager timelineManager, UiProjectRepository uiProjectRepository) {
        this.timelineManager = timelineManager;
        this.uiProjectRepository = uiProjectRepository;
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
        return ByteBufferToImageConverter.convertToJavaxImage(frame, width, height);
    }
}
