package com.helospark.tactview.core.timeline.image;

import java.io.File;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.api.LoadMetadata;
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
    private VisualMediaMetadata mediaMetadata;

    public ImageClip(VisualMediaSource mediaSource, VisualMediaMetadata metadata, TimelinePosition position, TimelineLength length) {
        super(metadata, new TimelineInterval(position, length), TimelineClipType.IMAGE);
        this.backingSource = mediaSource;
        this.mediaMetadata = metadata;
    }

    public ImageClip(ImageClip imageClip) {
        super(imageClip);
        this.backingSource = imageClip.backingSource;
        this.mediaMetadata = imageClip.mediaMetadata;
    }

    public ImageClip(VisualMediaMetadata metadata, VisualMediaSource videoSource, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(metadata, savedClip, loadMetadata);
        this.backingSource = videoSource;
        this.mediaMetadata = metadata;
    }

    @Override
    public ByteBuffer requestFrame(TimelinePosition position, int width, int height) {
        VideoMediaDataRequest request = VideoMediaDataRequest.builder()
                .withFile(new File(backingSource.backingFile))
                .withWidth(width)
                .withHeight(height)
                .withNumberOfFrames(1)
                .withMetadata(mediaMetadata)
                .build(); // todo: cache and scale
        MediaDataResponse result = backingSource.decoder.readFrames(request);
        return result.getFrames().get(0);
    }

    @Override
    public VisualMediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    public VisualMediaSource getMediaSource() {
        return backingSource;
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
