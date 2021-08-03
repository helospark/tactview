package com.helospark.tactview.core.timeline.subtimeline.audio;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.TemplateSaveHandler;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.subtimeline.TimelineManagerAccessorFactory;
import com.helospark.tactview.core.timeline.subtimeline.loadhelper.LoadData;
import com.helospark.tactview.core.timeline.subtimeline.loadhelper.SubtimelineLoadFileService;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class SubtimelineAudioClipFactory implements ClipFactory {
    public static final String ID = "subtimelineAudioClipFactory";

    private TimelineManagerAccessorFactory timelineManagerAccessorFactory;
    private SubtimelineLoadFileService subtimelineLoadFileService;
    private MessagingService messagingService;

    public SubtimelineAudioClipFactory(TimelineManagerAccessorFactory timelineManagerAccessorFactory, SubtimelineLoadFileService subtimelineLoadFileService, MessagingService messagingService) {
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.subtimelineLoadFileService = subtimelineLoadFileService;
        this.messagingService = messagingService;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return request.containsFile() && request.getFilePath().endsWith("." + TemplateSaveHandler.TEMPLATE_FILE_EXTENSION); // TODO: check content if there is track
    }

    @Override
    public MediaMetadata readMetadata(AddClipRequest request) {
        LoadData loadData = subtimelineLoadFileService.getLoadData(request, TemplateSaveHandler.AUDIO_TRACK_NODE);
        return SubtimelineAudioClip.readMetadata(loadData.tree, loadData.loadMetadata);
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        LoadData loadData = subtimelineLoadFileService.getLoadData(request, TemplateSaveHandler.AUDIO_TRACK_NODE);

        return new SubtimelineAudioClip(timelineManagerAccessorFactory, messagingService, loadData.tree, loadData.loadMetadata).cloneClip(CloneRequestMetadata.ofDefault());
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public TimelineClip restoreClip(JsonNode savedClip, LoadMetadata loadMetadata) {
        return new SubtimelineAudioClip(timelineManagerAccessorFactory, messagingService, savedClip, loadMetadata);
    }

}
