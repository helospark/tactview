package com.helospark.tactview.core.timeline.subtimeline.video;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
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
public class SubtimelineVisualClipFactory implements ClipFactory {
    public static final String ID = "subtimelineVisualClipFactory";

    private TimelineManagerAccessorFactory timelineManagerAccessorFactory;
    private SubtimelineLoadFileService subtimelineLoadFileService;
    private SubtimelineHelper subtimelineHelper;

    public SubtimelineVisualClipFactory(TimelineManagerAccessorFactory timelineManagerAccessorFactory, SubtimelineLoadFileService subtimelineLoadFileService, SubtimelineHelper subtimelineHelper) {
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.subtimelineLoadFileService = subtimelineLoadFileService;
        this.subtimelineHelper = subtimelineHelper;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return subtimelineHelper.containsSubtimeline(request, TemplateSaveAndLoadHandler.VIDEO_TRACK_NODE);
    }

    @Override
    public MediaMetadata readMetadata(AddClipRequest request) {
        LoadData loadData = subtimelineLoadFileService.getLoadData(request, TemplateSaveAndLoadHandler.VIDEO_TRACK_NODE);
        return SubtimelineHelper.readMetadata(loadData.tree.get(0), loadData.loadMetadata, SubtimelineVisualMetadata.class);
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        LoadData loadData = subtimelineLoadFileService.getLoadData(request, TemplateSaveAndLoadHandler.VIDEO_TRACK_NODE);

        return new SubtimelineVisualClip(timelineManagerAccessorFactory, subtimelineHelper, loadData.tree, loadData.loadMetadata).cloneClip(CloneRequestMetadata.ofDefault());
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public TimelineClip restoreClip(JsonNode savedClip, LoadMetadata loadMetadata) {
        return new SubtimelineVisualClip(timelineManagerAccessorFactory, subtimelineHelper, savedClip, loadMetadata);
    }

}
