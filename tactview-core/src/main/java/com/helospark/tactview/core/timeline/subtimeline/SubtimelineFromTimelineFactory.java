package com.helospark.tactview.core.timeline.subtimeline;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineChannelsState;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.subtimeline.audio.SubtimelineAudioClip;
import com.helospark.tactview.core.timeline.subtimeline.audio.SubtimelineAudioClipFactory;
import com.helospark.tactview.core.timeline.subtimeline.video.SubtimelineVisualClip;
import com.helospark.tactview.core.timeline.subtimeline.video.SubtimelineVisualClipFactory;
import com.helospark.tactview.core.timeline.subtimeline.video.SubtimelineVisualMetadata;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class SubtimelineFromTimelineFactory {
    private ProjectRepository projectRepository;
    private TimelineManagerAccessor timelineManager;
    private TimelineChannelsState timelineChannelsState;
    private TimelineManagerAccessorFactory timelineManagerAccessorFactory;
    private MessagingService messagingService;

    public SubtimelineFromTimelineFactory(ProjectRepository projectRepository, TimelineManagerAccessor timelineManager, TimelineChannelsState timelineChannelsState,
            TimelineManagerAccessorFactory timelineManagerAccessorFactory, MessagingService messagingService) {
        this.projectRepository = projectRepository;
        this.timelineManager = timelineManager;
        this.timelineChannelsState = timelineChannelsState;
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.messagingService = messagingService;
    }

    public SubtimelineVisualClip createSubtimelineVideoClipFromCurrentTimeline() {
        TimelineLength length = timelineManager.findEndPosition().toLength();
        SubtimelineVisualMetadata metadata = SubtimelineVisualMetadata.builder()
                .withWidth(projectRepository.getWidth())
                .withHeight(projectRepository.getHeight())
                .withResizable(true)
                .withLength(length)
                .build();

        SubtimelineVisualClip result = new SubtimelineVisualClip(metadata, timelineChannelsState.deepClone(CloneRequestMetadata.ofDefault()), timelineManagerAccessorFactory, messagingService,
                TimelinePosition.ofZero(),
                length);

        result.setCreatorFactoryId(SubtimelineVisualClipFactory.ID);

        return result;
    }

    public SubtimelineAudioClip createSubtimelineAudioClipFromCurrentTimeline() {
        TimelineLength length = timelineManager.findEndPosition().toLength();
        AudioMediaMetadata metadata = AudioMediaMetadata.builder()
                .withBitRate(timelineManager.findMaximumAudioBitRate())
                .withBytesPerSample(projectRepository.getBytesPerSample())
                .withChannels(projectRepository.getNumberOfChannels())
                .withLength(timelineManager.findEndPosition().toLength())
                .withSampleRate(projectRepository.getBytesPerSample())
                .build();

        SubtimelineAudioClip result = new SubtimelineAudioClip(metadata, timelineChannelsState.deepClone(CloneRequestMetadata.ofDefault()), timelineManagerAccessorFactory, messagingService,
                TimelinePosition.ofZero(),
                length);
        result.setCreatorFactoryId(SubtimelineAudioClipFactory.ID);
        return result;
    }

}
