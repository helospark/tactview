package com.helospark.tactview.core.timeline.subtimeline.audio;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.subtimeline.TimelineManagerAccessorFactory;

@Component
public class SubtimelineAudioClipFactory implements ClipFactory {

    public static final String ID = "subtimelineAudioClipFactory";

    private TimelineManagerAccessorFactory timelineManagerAccessorFactory;

    public SubtimelineAudioClipFactory(TimelineManagerAccessorFactory timelineManagerAccessorFactory) {
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return false;// TODO:
    }

    @Override
    public MediaMetadata readMetadata(AddClipRequest request) {
        return null;// TODO:
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        return null; // TODO:
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public TimelineClip restoreClip(JsonNode savedClip, LoadMetadata loadMetadata) {
        return new SubtimelineAudioClip(timelineManagerAccessorFactory, savedClip, loadMetadata);
    }

}
