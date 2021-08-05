package com.helospark.tactview.core.timeline.subtimeline.audio;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.TemplateSaveAndLoadHandler;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.subtimeline.TimelineManagerAccessorFactory;
import com.helospark.tactview.core.timeline.subtimeline.loadhelper.LoadData;
import com.helospark.tactview.core.timeline.subtimeline.loadhelper.SubtimelineLoadFileService;

@Component
public class SubtimelineAudioClipFactory implements ClipFactory {
    public static final String ID = "subtimelineAudioClipFactory";

    private TimelineManagerAccessorFactory timelineManagerAccessorFactory;
    private SubtimelineLoadFileService subtimelineLoadFileService;

    public SubtimelineAudioClipFactory(TimelineManagerAccessorFactory timelineManagerAccessorFactory, SubtimelineLoadFileService subtimelineLoadFileService) {
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.subtimelineLoadFileService = subtimelineLoadFileService;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return request.containsFile() && request.getFilePath().endsWith("." + TemplateSaveAndLoadHandler.TEMPLATE_FILE_EXTENSION); // TODO: check content if there is track
    }

    @Override
    public MediaMetadata readMetadata(AddClipRequest request) {
        LoadData loadData = subtimelineLoadFileService.getLoadData(request, TemplateSaveAndLoadHandler.AUDIO_TRACK_NODE);
        return SubtimelineAudioClip.readMetadata(loadData.tree, loadData.loadMetadata);
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        LoadData loadData = subtimelineLoadFileService.getLoadData(request, TemplateSaveAndLoadHandler.AUDIO_TRACK_NODE);

        return new SubtimelineAudioClip(timelineManagerAccessorFactory, loadData.tree, loadData.loadMetadata).cloneClip(CloneRequestMetadata.ofDefault());
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
