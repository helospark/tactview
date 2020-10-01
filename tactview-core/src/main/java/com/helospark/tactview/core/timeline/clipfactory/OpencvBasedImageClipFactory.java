package com.helospark.tactview.core.timeline.clipfactory;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.opencv.OpenCvImageDecorderDecorator;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualMediaSource;
import com.helospark.tactview.core.timeline.image.ImageClip;
import com.helospark.tactview.core.util.FileTypeProberUtil;

@Component
public class OpencvBasedImageClipFactory implements ClipFactory {
    private OpenCvImageDecorderDecorator mediaDecoder;

    public OpencvBasedImageClipFactory(OpenCvImageDecorderDecorator mediaDecoder) {
        this.mediaDecoder = mediaDecoder;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return request.containsFile() &&
                FileTypeProberUtil.isImageByContentType(request.getFile()) &&
                !request.getFile().getName().endsWith(".gif");
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        File file = request.getFile();
        TimelinePosition position = request.getPosition();
        VisualMediaSource mediaSource = new VisualMediaSource(file, mediaDecoder);
        ImageMetadata metadata = readMetadata(request.getFile());
        ImageClip result = new ImageClip(mediaSource, metadata, position, metadata.getLength());
        result.setCreatorFactoryId(getId());
        return result;
    }

    @Override
    public TimelineClip restoreClip(JsonNode savedClip, LoadMetadata loadMetadata) {
        File file = loadMetadata.resolveFilePath(savedClip.get("backingFile").asText());
        ImageMetadata metadata = readMetadata(file);
        VisualMediaSource videoSource = new VisualMediaSource(file, mediaDecoder);

        return new ImageClip(metadata, videoSource, savedClip, loadMetadata);
    }

    @Override
    public ImageMetadata readMetadata(AddClipRequest request) {
        return readMetadata(request.getFile());
    }

    private ImageMetadata readMetadata(File file) {
        return mediaDecoder.readMetadata(file);
    }

    @Override
    public String getId() {
        return "opencvImageFactory";
    }

}
