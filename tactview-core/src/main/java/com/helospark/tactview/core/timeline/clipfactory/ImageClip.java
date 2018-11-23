package com.helospark.tactview.core.timeline.clipfactory;

import java.io.File;
import java.nio.ByteBuffer;

import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.VideoMediaDataRequest;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualMediaSource;
import com.helospark.tactview.core.timeline.VisualTimelineClip;

// TODO this class is basically the same as VideoClip, maybe merge
public class ImageClip extends VisualTimelineClip {
    private VisualMediaSource mediaSource;
    private VisualMediaMetadata mediaMetadata;

    public ImageClip(VisualMediaSource mediaSource, VisualMediaMetadata metadata, TimelinePosition position, TimelineLength length) {
        super(metadata, new TimelineInterval(position, length), TimelineClipType.IMAGE);
        this.mediaSource = mediaSource;
        this.mediaMetadata = metadata;
    }

    public ImageClip(ImageClip imageClip) {
        super(imageClip);
        this.mediaSource = imageClip.mediaSource;
        this.mediaMetadata = imageClip.mediaMetadata;
    }

    @Override
    public ByteBuffer requestFrame(TimelinePosition position, int width, int height) {
        VideoMediaDataRequest request = VideoMediaDataRequest.builder()
                .withFile(new File(mediaSource.backingFile))
                .withWidth(width)
                .withHeight(height)
                .withNumberOfFrames(1)
                .withMetadata(mediaMetadata)
                .build(); // todo: cache and scale
        MediaDataResponse result = mediaSource.decoder.readFrames(request);
        return result.getFrames().get(0);
    }

    @Override
    public VisualMediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    public VisualMediaSource getMediaSource() {
        return mediaSource;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public TimelineClip cloneClip() {
        return new ImageClip(this);
    }

}
