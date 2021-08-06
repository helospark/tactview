package com.helospark.tactview.core.save;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
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
    private ProjectRepository projectRepository;

    public TemplateSaveAndLoadHandler(SubtimelineFromTimelineFactory subtimelineFromTimelineFactory, LightDiContext context,
            TimelineManagerAccessor timelineManagerAccessor, ProjectRepository projectRepository) {
        super(TEMPLATE_FILE_NAME, context);
        this.subtimelineFromTimelineFactory = subtimelineFromTimelineFactory;
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.projectRepository = projectRepository;
    }

    public void save(SaveTemplateRequest saveTemplateRequest) {
        File rootDirectory = createRootDirectory();

        Map<String, Object> result = new LinkedHashMap<>();

        SaveMetadata saveMetadata = new SaveMetadata(saveTemplateRequest.isPackageAllContent());

        queryAdditionalDataToSave(result, saveMetadata, saveTemplateRequest);

        SaveRequest saveRequest = SaveRequest
                .builder()
                .withFileName(saveTemplateRequest.getFileName())
                .withPackageAllContent(saveTemplateRequest.isPackageAllContent())
                .build();

        createSavePackageFromResultt(saveRequest, rootDirectory, result, saveMetadata);

        deleteDirectory(rootDirectory);
    }

    protected void queryAdditionalDataToSave(Map<String, Object> result, SaveMetadata saveMetadata, SaveTemplateRequest saveTemplateRequest) {

        if (projectRepository.isVideoInitialized()) {
            SubtimelineVisualClip videoClip = subtimelineFromTimelineFactory.createSubtimelineVideoClipFromCurrentTimeline(saveTemplateRequest.getExposedDescriptors());
            result.put(VIDEO_TRACK_NODE, videoClip.generateSavedContent(saveMetadata));
        }
        if (projectRepository.isAudioInitialized()) {
            SubtimelineAudioClip audioClip = subtimelineFromTimelineFactory.createSubtimelineAudioClipFromCurrentTimeline(saveTemplateRequest.getExposedDescriptors());
            result.put(AUDIO_TRACK_NODE, audioClip.generateSavedContent(saveMetadata));
        }

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

        if (tree.get(VIDEO_TRACK_NODE) != null) {
            timelineManagerAccessor.loadFrom(tree.get(VIDEO_TRACK_NODE), loadMetadata);
        } else {
            timelineManagerAccessor.loadFrom(tree.get(AUDIO_TRACK_NODE), loadMetadata);
        }
    }
}
