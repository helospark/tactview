package com.helospark.tactview.ui.javafx;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.audio.JavaByteArrayConverter;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.scene.image.Image;

@Component
public class PlaybackController {
    private TimelineManager timelineManager;
    private UiProjectRepository uiProjectRepository;
    private ByteBufferToJavaFxImageConverter byteBufferToImageConverter;
    private JavaByteArrayConverter javaByteArrayConverter;
    private UiPlaybackPreferenceRepository uiPlaybackPreferenceRepository;

    public PlaybackController(TimelineManager timelineManager, UiProjectRepository uiProjectRepository,
            ByteBufferToJavaFxImageConverter byteBufferToImageConverter, JavaByteArrayConverter javaByteArrayConverter,
            UiPlaybackPreferenceRepository uiPlaybackPreferenceRepository) {
        this.timelineManager = timelineManager;
        this.uiProjectRepository = uiProjectRepository;
        this.byteBufferToImageConverter = byteBufferToImageConverter;
        this.javaByteArrayConverter = javaByteArrayConverter;
        this.uiPlaybackPreferenceRepository = uiPlaybackPreferenceRepository;
    }

    public JavaDisplayableAudioVideoFragment getFrameAt(TimelinePosition position) {
        Integer width = uiProjectRepository.getPreviewWidth();
        Integer height = uiProjectRepository.getPreviewHeight();
        TimelineManagerFramesRequest request = TimelineManagerFramesRequest.builder()
                .withFrameBufferSize(1)
                .withPosition(position)
                .withScale(uiProjectRepository.getScaleFactor())
                .withPreviewWidth(width)
                .withPreviewHeight(height)
                .withNeedSound(!uiPlaybackPreferenceRepository.isMute())
                .build();
        AudioVideoFragment frame = timelineManager.getFrames(request);
        Image javafxImage = byteBufferToImageConverter.convertToJavafxImage(frame.getVideoResult().getBuffer(), width, height);
        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frame.getVideoResult().getBuffer());

        byte[] buffer = javaByteArrayConverter.convert(frame.getAudioResult(), 2, 44100, 1); // move data to repository

        for (var audioFrame : frame.getAudioResult().getChannels()) {
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(audioFrame);
        }

        return new JavaDisplayableAudioVideoFragment(javafxImage, buffer);
    }
}
