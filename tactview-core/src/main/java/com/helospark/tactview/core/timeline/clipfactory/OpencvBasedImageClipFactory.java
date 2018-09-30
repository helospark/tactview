package com.helospark.tactview.core.timeline.clipfactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.opencv.OpenCvImageDecorderDecorator;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.MediaSource;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;

@Component
public class OpencvBasedImageClipFactory implements ClipFactory {
    private OpenCvImageDecorderDecorator mediaDecoder;

    public OpencvBasedImageClipFactory(OpenCvImageDecorderDecorator mediaDecoder) {
        this.mediaDecoder = mediaDecoder;
    }

    @Override
    public boolean doesSupport(File file) {
        try {
            return Files.probeContentType(file.toPath()).contains("image/");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public TimelineClip createClip(File file, TimelinePosition position) {
        MediaSource mediaSource = new MediaSource(file, mediaDecoder);
        ImageMetadata metadata = readMetadata(file);
        return new ImageClip(mediaSource, metadata, position, metadata.getLength());
    }

    @Override
    public ImageMetadata readMetadata(File file) {
        return mediaDecoder.readMetadata(file);
    }

}
