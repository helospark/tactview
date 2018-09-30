package com.helospark.tactview.core.timeline.clipfactory;

import java.io.File;
import java.nio.ByteBuffer;

import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.MediaDataRequest;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.MediaSource;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;

public class ImageClip extends VisualTimelineClip {
    private MediaSource mediaSource;
    private ImageMetadata mediaMetadata;

    public ImageClip(MediaSource mediaSource, ImageMetadata metadata, TimelinePosition position, TimelineLength length) {
        super(metadata, new TimelineInterval(position, length), TimelineClipType.IMAGE);
        this.mediaSource = mediaSource;
        this.mediaMetadata = metadata;
    }

    @Override
    public ByteBuffer requestFrame(TimelinePosition position, int width, int height) {
        MediaDataRequest request = MediaDataRequest.builder()
                .withFile(new File(mediaSource.backingFile))
                .withWidth(width)
                .withHeight(height)
                .withNumberOfFrames(1)
                .build(); // todo: cache and scale
        MediaDataResponse result = mediaSource.decoder.readFrames(request);
        return result.getVideoFrames().get(0);
    }

    @Override
    public VisualMediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

}
