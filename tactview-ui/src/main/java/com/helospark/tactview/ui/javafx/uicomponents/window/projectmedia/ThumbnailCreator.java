package com.helospark.tactview.ui.javafx.uicomponents.window.projectmedia;

import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.MemoryManager;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.ui.javafx.uicomponents.pattern.AudioImagePatternService;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

@Component
public class ThumbnailCreator {
    private MemoryManager memoryManager;
    private ByteBufferToJavaFxImageConverter imageConverter;
    private AudioImagePatternService audioImagePatternService;

    public ThumbnailCreator(MemoryManager memoryManager, ByteBufferToJavaFxImageConverter imageConverter, AudioImagePatternService audioImagePatternService) {
        this.memoryManager = memoryManager;
        this.imageConverter = imageConverter;
        this.audioImagePatternService = audioImagePatternService;
    }

    public Image getImageFor(List<TimelineClip> templateClips, TimelinePosition timelinePosition, int width) {
        Optional<VisualTimelineClip> visualClip = getFirstClipOfType(templateClips, VisualTimelineClip.class);
        Optional<AudibleTimelineClip> audioClip = getFirstClipOfType(templateClips, AudibleTimelineClip.class);

        if (visualClip.isPresent()) {
            VisualMediaMetadata metadata = visualClip.get().getMediaMetadata();
            double aspectRatio = (double) metadata.getWidth() / metadata.getHeight();
            GetFrameRequest getFrameRequest = GetFrameRequest.builder()
                    .withApplyEffects(true)
                    .withExpectedWidth(width)
                    .withExpectedHeight((int) (width / aspectRatio))
                    .withPosition(timelinePosition)
                    .withRelativePosition(timelinePosition)
                    .withScale((double) width / metadata.getWidth())
                    .withUseApproximatePosition(true)
                    .build();
            ReadOnlyClipImage frame = visualClip.get().getFrame(getFrameRequest);
            Image result = imageConverter.convertToJavafxImage(frame.getBuffer(), frame.getWidth(), frame.getHeight());
            memoryManager.returnBuffer(frame.getBuffer());
            return result;
        } else if (audioClip.isPresent()) {
            AudibleTimelineClip actualClip = audioClip.get();
            double endPosition = actualClip.getInterval().getLength().getSeconds().doubleValue();
            return audioImagePatternService.createAudioImagePattern(actualClip, width, AudioImagePatternService.DEFAULT_HEIGHT, 0.0, endPosition);
        } else {
            return new WritableImage(width, width);
        }
    }

    private <T extends TimelineClip> Optional<T> getFirstClipOfType(List<TimelineClip> templateClips, Class<T> type) {
        return templateClips.stream().filter(clip -> type.isAssignableFrom(clip.getClass())).map(clip -> type.cast(clip)).findFirst();
    }
}
