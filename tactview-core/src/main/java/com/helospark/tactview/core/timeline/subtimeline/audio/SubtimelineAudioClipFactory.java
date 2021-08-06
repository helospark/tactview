package com.helospark.tactview.core.timeline.subtimeline.audio;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.TemplateSaveAndLoadHandler;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.subtimeline.SubtimelineHelper;
import com.helospark.tactview.core.timeline.subtimeline.TimelineManagerAccessorFactory;
import com.helospark.tactview.core.timeline.subtimeline.loadhelper.LoadData;
import com.helospark.tactview.core.timeline.subtimeline.loadhelper.SubtimelineLoadFileService;

@Component
public class SubtimelineAudioClipFactory implements ClipFactory {
    public static final String ID = "subtimelineAudioClipFactory";

    private TimelineManagerAccessorFactory timelineManagerAccessorFactory;
    private SubtimelineLoadFileService subtimelineLoadFileService;
    private SubtimelineHelper subtimelineHelper;

    public SubtimelineAudioClipFactory(TimelineManagerAccessorFactory timelineManagerAccessorFactory, SubtimelineLoadFileService subtimelineLoadFileService, SubtimelineHelper subtimelineHelper) {
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.subtimelineLoadFileService = subtimelineLoadFileService;
        this.subtimelineHelper = subtimelineHelper;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return subtimelineHelper.containsSubtimeline(request, TemplateSaveAndLoadHandler.AUDIO_TRACK_NODE);
    }

    @Override
    public MediaMetadata readMetadata(AddClipRequest request) {
        LoadData loadData = subtimelineLoadFileService.getLoadData(request, TemplateSaveAndLoadHandler.AUDIO_TRACK_NODE);
        return SubtimelineHelper.readMetadata(loadData.tree, loadData.loadMetadata, AudioMediaMetadata.class);
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        LoadData loadData = subtimelineLoadFileService.getLoadData(request, TemplateSaveAndLoadHandler.AUDIO_TRACK_NODE);

        return new SubtimelineAudioClip(timelineManagerAccessorFactory, subtimelineHelper, loadData.tree, loadData.loadMetadata).cloneClip(CloneRequestMetadata.ofDefault());
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public TimelineClip restoreClip(JsonNode savedClip, LoadMetadata loadMetadata) {
        return new SubtimelineAudioClip(timelineManagerAccessorFactory, subtimelineHelper, savedClip, loadMetadata);
    }

}
