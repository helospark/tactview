package com.helospark.tactview.ui.javafx;

import java.nio.ByteBuffer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.scene.image.Image;

@Component
public class PlaybackController {
    private TimelineManager timelineManager;
    private UiSettingsRepository settingsRepository;

    public PlaybackController(TimelineManager timelineManager) {
        this.timelineManager = timelineManager;
    }

    public Image getFrameAt(TimelinePosition position) {
        Integer width = 320;//settingsRepository.getAs("core.preview.width", Integer.class);
        Integer height = 260;//settingsRepository.getAs("core.preview.height", Integer.class);
        TimelineManagerFramesRequest request = TimelineManagerFramesRequest.builder()
                .withFrameBufferSize(1)
                .withPosition(position)
                .withPreviewWidth(width)
                .withPreviewHeight(height)
                .build();
        ByteBuffer frame = timelineManager.getFrames(request);
        return ByteBufferToImageConverter.convertToJavaxImage(frame, width, height);
    }
}
