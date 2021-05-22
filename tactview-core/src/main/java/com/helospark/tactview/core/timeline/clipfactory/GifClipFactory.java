package com.helospark.tactview.core.timeline.clipfactory;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.decoder.gif.GifMediaDecoder;
import com.helospark.tactview.core.decoder.gif.GifVideoMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.core.timeline.VisualMediaSource;
import com.helospark.tactview.core.timeline.effect.rotate.RotateService;

@Component
public class GifClipFactory implements ClipFactory {
    private GifMediaDecoder gifMediaDecoder;
    private RotateService rotateService;

    public GifClipFactory(GifMediaDecoder gifMediaDecoder, RotateService rotateService) {
        this.gifMediaDecoder = gifMediaDecoder;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return request.containsFile() && request.getFile().getName().endsWith(".gif");
    }

    @Override
    public MediaMetadata readMetadata(AddClipRequest request) {
        return gifMediaDecoder.readMetadata(request.getFile());
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        File file = request.getFile();
        TimelinePosition position = request.getPosition();
        GifVideoMetadata metadata = gifMediaDecoder.readMetadata(file);

        VisualMediaSource videoSource = new VisualMediaSource(file, gifMediaDecoder);
        VideoClip videoClip = new VideoClip(metadata, videoSource, position, metadata.getLength(), rotateService);

        videoClip.setCreatorFactoryId(getId());

        return videoClip;
    }

    @Override
    public TimelineClip restoreClip(JsonNode savedClip, LoadMetadata loadMetadata) {
        File file = loadMetadata.resolveFilePath(savedClip.get("backingFile").asText());
        GifVideoMetadata metadata = gifMediaDecoder.readMetadata(file);
        VisualMediaSource videoSource = new VisualMediaSource(file, gifMediaDecoder);

        return new VideoClip(metadata, videoSource, savedClip, loadMetadata, rotateService);
    }

    @Override
    public String getId() {
        return "gifClipFactory";
    }

}
