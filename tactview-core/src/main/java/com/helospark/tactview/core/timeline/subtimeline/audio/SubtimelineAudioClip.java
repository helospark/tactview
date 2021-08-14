package com.helospark.tactview.core.timeline.subtimeline.audio;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.AudioRequest;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineChannelsState;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.subtimeline.ExposedDescriptorDescriptor;
import com.helospark.tactview.core.timeline.subtimeline.SubtimelineHelper;
import com.helospark.tactview.core.timeline.subtimeline.TimelineManagerAccessorFactory;

public class SubtimelineAudioClip extends AudibleTimelineClip {
    private TimelineManagerAccessor timelineManagerAccessor;
    private TimelineManagerRenderService timelineManagerRenderService;
    private TimelineChannelsState timelineState;
    private TimelineManagerAccessorFactory timelineManagerAccessorFactory;
    private SubtimelineHelper subtimelineHelper;

    private Set<ExposedDescriptorDescriptor> enabledDescriptors = new HashSet<>();

    public SubtimelineAudioClip(AudioMediaMetadata metadata, TimelineChannelsState timelineState, TimelineManagerAccessorFactory timelineManagerAccessorFactory,
            SubtimelineHelper subtimelineHelper,
            Set<ExposedDescriptorDescriptor> descriptorIds,
            TimelinePosition position,
            TimelineLength length) {
        super(new TimelineInterval(position, length), metadata);
        this.timelineState = timelineState;
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.timelineManagerAccessor = timelineManagerAccessorFactory.createAccessor(timelineState);
        this.timelineManagerRenderService = timelineManagerAccessorFactory.createRenderService(timelineState, timelineManagerAccessor);
        this.mediaMetadata = metadata;
        this.enabledDescriptors = descriptorIds;
    }

    public SubtimelineAudioClip(SubtimelineAudioClip clip, CloneRequestMetadata cloneRequestMetadata) {
        super(clip, cloneRequestMetadata);
        this.subtimelineHelper = clip.subtimelineHelper;
        this.timelineManagerAccessorFactory = clip.timelineManagerAccessorFactory;
        this.timelineState = clip.timelineState.deepClone(cloneRequestMetadata);
        this.timelineManagerAccessor = clip.timelineManagerAccessorFactory.createAccessor(timelineState);
        this.timelineManagerRenderService = clip.timelineManagerAccessorFactory.createRenderService(timelineState, timelineManagerAccessor);

        this.enabledDescriptors = subtimelineHelper.copyExposedDescriptors(cloneRequestMetadata, clip.enabledDescriptors);
    }

    public SubtimelineAudioClip(TimelineManagerAccessorFactory timelineManagerAccessorFactory, SubtimelineHelper subtimelineHelper, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(SubtimelineHelper.readMetadata(savedClip, loadMetadata, AudioMediaMetadata.class), savedClip, loadMetadata);
        this.subtimelineHelper = subtimelineHelper;
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.timelineState = new TimelineChannelsState();
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.timelineManagerAccessor = timelineManagerAccessorFactory.createAccessor(timelineState);
        this.timelineManagerRenderService = timelineManagerAccessorFactory.createRenderService(timelineState, timelineManagerAccessor);

        try {
            timelineManagerAccessor.loadFrom(savedClip, loadMetadata);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent, SaveMetadata saveMetadata) {
        this.timelineManagerAccessor.generateSavedContent(savedContent, saveMetadata);

        savedContent.put("metadata", this.getMediaMetadata());
    }

    @Override
    protected AudioFrameResult requestAudioFrameInternal(AudioRequest audioRequest) {
        TimelineManagerFramesRequest channelFrameRequest = TimelineManagerFramesRequest.builder()
                .withAudioBytesPerSample(Optional.of(audioRequest.getBytesPerSample()))
                .withAudioLength(Optional.of(audioRequest.getLength()))
                .withAudioSampleRate(Optional.of(audioRequest.getSampleRate()))
                .withPosition(audioRequest.getPosition())
                .withNeedSound(true)
                .withNeedVideo(false)
                .withPreviewWidth(800) // TODO: find why these dummy values are needed
                .withPreviewHeight(600)
                .build();

        AudioVideoFragment audioVideoFragment = timelineManagerRenderService.getFrame(channelFrameRequest).getAudioVideoFragment();

        audioVideoFragment.freeVideoResult();

        return audioVideoFragment.getAudioResult();
    }

    @Override
    public AudioMediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new SubtimelineAudioClip(this, cloneRequestMetadata);
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        subtimelineHelper.addDescriptorsFromTimeline(result, this.timelineManagerAccessor, enabledDescriptors);

        return result;
    }

}
