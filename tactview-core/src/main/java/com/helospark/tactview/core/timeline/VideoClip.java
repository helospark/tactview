package com.helospark.tactview.core.timeline;

import static com.helospark.tactview.core.timeline.TimelineClipType.VIDEO;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.VideoMediaDataRequest;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.util.StaticObjectMapper;

public class VideoClip extends VisualTimelineClip {
    private VisualMediaMetadata mediaMetadata;
    private TimelinePosition startPosition;

    public VideoClip(VisualMediaMetadata mediaMetadata, VisualMediaSource backingSource, TimelinePosition startPosition, TimelineLength length) {
        super(mediaMetadata, new TimelineInterval(startPosition, length), VIDEO);
        this.mediaMetadata = mediaMetadata;
        this.backingSource = backingSource;
        this.startPosition = startPosition;
    }

    public VideoClip(VideoClip clip, CloneRequestMetadata cloneRequestMetadata) {
        super(clip, cloneRequestMetadata);
        this.mediaMetadata = clip.mediaMetadata;
        this.backingSource = clip.backingSource;
        this.startPosition = clip.startPosition;
    }

    public VideoClip(VisualMediaMetadata metadata, VisualMediaSource videoSource, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(metadata, savedClip, loadMetadata);
        this.mediaMetadata = metadata;
        this.backingSource = videoSource;
        this.startPosition = StaticObjectMapper.toValue(savedClip, loadMetadata, "startPosition", TimelinePosition.class);
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent) {
        super.generateSavedContentInternal(savedContent);
        savedContent.put("startPosition", startPosition);
    }

    @Override
    public ByteBuffer requestFrame(RequestFrameParameter frameRequest) {
        VideoMediaDataRequest request = VideoMediaDataRequest.builder()
                .withFile(new File(backingSource.backingFile))
                .withHeight(frameRequest.getHeight())
                .withWidth(frameRequest.getWidth())
                .withMetadata(mediaMetadata)
                .withStart(frameRequest.getPosition())
                .withNumberOfFrames(1)
                .withUseApproximatePosition(frameRequest.useApproximatePosition())
                .build();
        return backingSource.decoder.readFrames(request)
                .getFrames()
                .get(0);
    }

    @Override
    public VisualMediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    public VisualMediaSource getBackingSource() {
        return backingSource;
    }

    public TimelinePosition getStartPosition() {
        return startPosition;
    }

    @Override
    public boolean isResizable() {
        return mediaMetadata.isResizable();
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new VideoClip(this, cloneRequestMetadata);
    }

}
