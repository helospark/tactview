package com.helospark.tactview.core.timeline.clipfactory;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.decoder.gif.GifMediaDecoder;
import com.helospark.tactview.core.decoder.gif.GifVideoMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.core.timeline.VisualMediaSource;

@Component
public class GifClipFactory implements ClipFactory {
    private GifMediaDecoder gifMediaDecoder;

    public GifClipFactory(GifMediaDecoder gifMediaDecoder) {
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
        VideoClip videoClip = new VideoClip(metadata, videoSource, position, metadata.getLength());

        videoClip.setCreatorFactoryId(getId());

        return videoClip;
    }

    @Override
    public TimelineClip restoreClip(JsonNode savedClip) {
        File file = new File(savedClip.get("backingFile").asText());
        GifVideoMetadata metadata = gifMediaDecoder.readMetadata(file);
        VisualMediaSource videoSource = new VisualMediaSource(file, gifMediaDecoder);

        return new VideoClip(metadata, videoSource, savedClip);
    }

    @Override
    public String getId() {
        return "gifClipFactory";
    }

}
