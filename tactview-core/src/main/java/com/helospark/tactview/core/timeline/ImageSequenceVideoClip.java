package com.helospark.tactview.core.timeline;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;

public class ImageSequenceVideoClip extends VideoClip {

    public ImageSequenceVideoClip(VideoClip clip, CloneRequestMetadata cloneRequestMetadata) {
        super(clip, cloneRequestMetadata);
    }

    public ImageSequenceVideoClip(VisualMediaMetadata metadata, VisualMediaSource videoSource, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(metadata, videoSource, savedClip, loadMetadata);
    }

    public ImageSequenceVideoClip(VisualMediaMetadata mediaMetadata, VisualMediaSource backingSource, TimelinePosition startPosition, TimelineLength length) {
        super(mediaMetadata, backingSource, startPosition, length);
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent) {
        super.generateSavedContentInternal(savedContent);
        savedContent.put("fps", ((VideoMetadata) mediaMetadata).getFps());
    }
}
