package com.helospark.tactview.core.timeline;

import static com.helospark.tactview.core.timeline.TimelineClipType.VIDEO;

import java.io.File;
import java.nio.ByteBuffer;

import com.helospark.tactview.core.decoder.VideoMediaDataRequest;
import com.helospark.tactview.core.decoder.VideoMetadata;

public class VideoClip extends VisualTimelineClip {
    private VideoMetadata mediaMetadata;
    private VisualMediaSource backingSource;
    private TimelinePosition startPosition;

    public VideoClip(VideoMetadata mediaMetadata, VisualMediaSource backingSource, TimelinePosition startPosition, TimelineLength length) {
        super(mediaMetadata, new TimelineInterval(startPosition, length), VIDEO);
        this.mediaMetadata = mediaMetadata;
        this.backingSource = backingSource;
        this.startPosition = startPosition;
    }

    @Override
    public ByteBuffer requestFrame(TimelinePosition position, int width, int height) {
        VideoMediaDataRequest request = VideoMediaDataRequest.builder()
                .withFile(new File(backingSource.backingFile))
                .withHeight(height)
                .withWidth(width)
                .withMetadata(mediaMetadata)
                .withStart(position)
                .withNumberOfFrames(1)
                .build();
        return backingSource.decoder.readFrames(request).getFrames().get(0);
    }

    @Override
    public VideoMetadata getMediaMetadata() {
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
        return false;
    }

    @Override
    protected TimelineClip cloneClip() {
        return new VideoClip(mediaMetadata, backingSource, this.interval.getStartPosition(), this.interval.getLength());
    }
}
