package com.helospark.tactview.core.timeline.clipfactory;

import java.io.File;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.decoder.ffmpeg.FFmpegBasedMediaDecoderDecorator;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.core.timeline.VideoSource;

@Component
public class FFmpegClipFactory implements ClipFactory {
    private FFmpegBasedMediaDecoderDecorator mediaDecoder;

    public FFmpegClipFactory(FFmpegBasedMediaDecoderDecorator mediaDecoder) {
        this.mediaDecoder = mediaDecoder;
    }

    @Override
    public TimelineClip createClip(File file, TimelinePosition position) {
        MediaMetadata metadata = mediaDecoder.readMetadata(file);
        VideoSource videoSource = new VideoSource(file, mediaDecoder);
        return new VideoClip(metadata, videoSource, position, metadata.getLength());
    }

    @Override
    public boolean doesSupport(File file) {
        return true; // TODO: based on format
    }

}
