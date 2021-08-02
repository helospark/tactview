package com.helospark.tactview.core.save;

import java.util.Map;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.subtimeline.SubtimelineFromTimelineFactory;
import com.helospark.tactview.core.timeline.subtimeline.audio.SubtimelineAudioClip;
import com.helospark.tactview.core.timeline.subtimeline.video.SubtimelineVisualClip;

@Component
public class TemplateSaveHandler extends AbstractSaveHandler {
    public static final String AUDIO_TRACK_NODE = "audioTrack";
    public static final String VIDEO_TRACK_NODE = "videoTrack";
    public static final String TEMPLATE_FILE_NAME = "template.json";
    public static final String TEMPLATE_FILE_EXTENSION = "tvt";
    private SubtimelineFromTimelineFactory subtimelineFromTimelineFactory;

    public TemplateSaveHandler(SubtimelineFromTimelineFactory subtimelineFromTimelineFactory) {
        super(TEMPLATE_FILE_NAME);
        this.subtimelineFromTimelineFactory = subtimelineFromTimelineFactory;
    }

    @Override
    protected void queryDataToSave(Map<String, Object> result, SaveMetadata saveMetadata) {
        SubtimelineVisualClip videoClip = subtimelineFromTimelineFactory.createSubtimelineVideoClipFromCurrentTimeline();
        SubtimelineAudioClip audioClip = subtimelineFromTimelineFactory.createSubtimelineAudioClipFromCurrentTimeline();

        result.put(VIDEO_TRACK_NODE, videoClip.generateSavedContent(saveMetadata));
        result.put(AUDIO_TRACK_NODE, audioClip.generateSavedContent(saveMetadata));
    }

}
