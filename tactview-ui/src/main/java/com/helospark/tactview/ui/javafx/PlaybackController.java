package com.helospark.tactview.ui.javafx;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
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
    private ProjectRepository projectRepository;
    private ByteBufferToJavaFxImageConverter byteBufferToImageConverter;
    private JavaByteArrayConverter javaByteArrayConverter;
    private UiPlaybackPreferenceRepository uiPlaybackPreferenceRepository;

    public PlaybackController(TimelineManager timelineManager, UiProjectRepository uiProjectRepository, ProjectRepository projectRepository,
            ByteBufferToJavaFxImageConverter byteBufferToImageConverter, JavaByteArrayConverter javaByteArrayConverter,
            UiPlaybackPreferenceRepository uiPlaybackPreferenceRepository) {
        this.timelineManager = timelineManager;
        this.uiProjectRepository = uiProjectRepository;
        this.byteBufferToImageConverter = byteBufferToImageConverter;
        this.javaByteArrayConverter = javaByteArrayConverter;
        this.projectRepository = projectRepository;
    }

    public JavaDisplayableAudioVideoFragment getVideoFrameAt(TimelinePosition position) {
        Integer width = uiProjectRepository.getPreviewWidth();
        Integer height = uiProjectRepository.getPreviewHeight();
        TimelineManagerFramesRequest request = TimelineManagerFramesRequest.builder()
                .withPosition(position)
                .withScale(uiProjectRepository.getScaleFactor())
                .withPreviewWidth(width)
                .withPreviewHeight(height)
                .withNeedSound(false)
                .withNeedVideo(true)
                .build();
        AudioVideoFragment frame = timelineManager.getFrame(request);
        Image javafxImage = byteBufferToImageConverter.convertToJavafxImage(frame.getVideoResult().getBuffer(), width, height);

        frame.free();

        return new JavaDisplayableAudioVideoFragment(javafxImage, new byte[0]);
    }

    public byte[] getAudioFrameAt(TimelinePosition position, int samples) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i = 0; i < samples; ++i) {
                Integer width = uiProjectRepository.getPreviewWidth();
                Integer height = uiProjectRepository.getPreviewHeight();
                TimelineManagerFramesRequest request = TimelineManagerFramesRequest.builder()
                        .withPosition(position.add(projectRepository.getFrameTime().multiply(BigDecimal.valueOf(i))))
                        .withScale(uiProjectRepository.getScaleFactor())
                        .withPreviewWidth(width)
                        .withPreviewHeight(height)
                        .withNeedSound(true)
                        .withNeedVideo(false)
                        .build();
                AudioVideoFragment frame = timelineManager.getFrame(request);
                byte[] buffer = javaByteArrayConverter.convert(frame.getAudioResult(), 2, 44100, 1); // move data to repository

                frame.free();

                baos.write(buffer);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
