package com.helospark.tactview.core.timeline.clipfactory;

import java.io.File;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.ffmpeg.FFmpegBasedMediaDecoderDecorator;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.MediaSource;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VideoClip;

@Component
@Order(value = Integer.MAX_VALUE)
public class FFmpegClipFactory implements ClipFactory {
    private FFmpegBasedMediaDecoderDecorator mediaDecoder;

    public FFmpegClipFactory(FFmpegBasedMediaDecoderDecorator mediaDecoder) {
        this.mediaDecoder = mediaDecoder;
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        File file = request.getFile();
        TimelinePosition position = request.getPosition();
        VideoMetadata metadata = mediaDecoder.readMetadata(file);
        MediaSource videoSource = new MediaSource(file, mediaDecoder);
        return new VideoClip(metadata, videoSource, position, metadata.getLength());
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return request.containsFile();// TODO: based on format
    }

    @Override
    public VisualMediaMetadata readMetadata(AddClipRequest request) {
        return mediaDecoder.readMetadata(request.getFile());
    }

}
