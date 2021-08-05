package com.helospark.tactview.core.save;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.subtimeline.SubtimelineFromTimelineFactory;
import com.helospark.tactview.core.timeline.subtimeline.audio.SubtimelineAudioClip;
import com.helospark.tactview.core.timeline.subtimeline.video.SubtimelineVisualClip;

@Component
public class TemplateSaveAndLoadHandler extends AbstractSaveHandler {
    public static final String AUDIO_TRACK_NODE = "audioTrack";
    public static final String VIDEO_TRACK_NODE = "videoTrack";
    public static final String TEMPLATE_FILE_NAME = "template.json";
    public static final String TEMPLATE_FILE_EXTENSION = "tvt";
    private SubtimelineFromTimelineFactory subtimelineFromTimelineFactory;
    private TimelineManagerAccessor timelineManagerAccessor;

    public TemplateSaveAndLoadHandler(SubtimelineFromTimelineFactory subtimelineFromTimelineFactory, LightDiContext context,
            TimelineManagerAccessor timelineManagerAccessor) {
        super(TEMPLATE_FILE_NAME, context);
        this.subtimelineFromTimelineFactory = subtimelineFromTimelineFactory;
        this.timelineManagerAccessor = timelineManagerAccessor;
    }

    @Override
    protected void queryAdditionalDataToSave(Map<String, Object> result, SaveMetadata saveMetadata) {
        Set<String> asd = this.timelineManagerAccessor.getChannels()
                .stream()
                .flatMap(a -> a.getAllClips().stream())
                .flatMap(a -> a.getDescriptors().stream())
                .map(a -> a.getKeyframeableEffect().getId())
                .collect(Collectors.toSet());

        SubtimelineVisualClip videoClip = subtimelineFromTimelineFactory.createSubtimelineVideoClipFromCurrentTimeline(asd);
        SubtimelineAudioClip audioClip = subtimelineFromTimelineFactory.createSubtimelineAudioClipFromCurrentTimeline();

        result.put(VIDEO_TRACK_NODE, videoClip.generateSavedContent(saveMetadata));
        result.put(AUDIO_TRACK_NODE, audioClip.generateSavedContent(saveMetadata));

        context.getListOfBeans(SaveLoadContributor.class)
                .stream()
                .filter(a -> !a.getClass().equals(TimelineManagerAccessor.class))
                .forEach(a -> a.generateSavedContent(result, saveMetadata));
    }

    @Override
    protected void loadAdditionalElements(JsonNode tree, LoadMetadata loadMetadata) {
        context.getListOfBeans(SaveLoadContributor.class)
                .stream()
                .filter(a -> !a.getClass().equals(TimelineManagerAccessor.class))
                .forEach(a -> a.loadFrom(tree, loadMetadata));

        timelineManagerAccessor.loadFrom(tree.get(VIDEO_TRACK_NODE), loadMetadata);
    }
}
