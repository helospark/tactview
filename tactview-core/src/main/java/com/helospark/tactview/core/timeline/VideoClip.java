package com.helospark.tactview.core.timeline;

import static com.helospark.tactview.core.timeline.TimelineClipType.VIDEO;

import java.io.File;
import java.nio.ByteBuffer;

import com.helospark.tactview.core.decoder.MediaDataRequest;
import com.helospark.tactview.core.decoder.VideoMetadata;

public class VideoClip extends VisualTimelineClip {
    private VideoMetadata mediaMetadata;
    private MediaSource backingSource;
    private TimelinePosition startPosition;

    public VideoClip(VideoMetadata mediaMetadata, MediaSource backingSource, TimelinePosition startPosition, TimelineLength length) {
        super(mediaMetadata, new TimelineInterval(startPosition, length), VIDEO);
        this.mediaMetadata = mediaMetadata;
        this.backingSource = backingSource;
        this.startPosition = startPosition;
    }

    @Override
    public ByteBuffer requestFrame(TimelinePosition position, int width, int height) {
        MediaDataRequest request = MediaDataRequest.builder()
                .withFile(new File(backingSource.backingFile))
                .withHeight(height)
                .withWidth(width)
                .withMetadata(mediaMetadata)
                .withStart(position)
                .withNumberOfFrames(1)
                .build();
        return backingSource.decoder.readFrames(request).getVideoFrames().get(0);
    }

    @Override
    public VideoMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    public MediaSource getBackingSource() {
        return backingSource;
    }

    public TimelinePosition getStartPosition() {
        return startPosition;
    }

    @Override
    public boolean isResizable() {
        return false;
    }
}
