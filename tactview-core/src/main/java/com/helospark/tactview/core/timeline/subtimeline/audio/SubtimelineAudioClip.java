package com.helospark.tactview.core.timeline.subtimeline.audio;

import java.util.Map;
import java.util.Optional;

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
import com.helospark.tactview.core.timeline.subtimeline.TimelineManagerAccessorFactory;

public class SubtimelineAudioClip extends AudibleTimelineClip {
    private TimelineManagerAccessor timelineManagerAccessor;
    private TimelineManagerRenderService timelineManagerRenderService;
    private TimelineChannelsState timelineState;
    private TimelineManagerAccessorFactory timelineManagerAccessorFactory;

    public SubtimelineAudioClip(AudioMediaMetadata metadata, TimelineChannelsState timelineState, TimelineManagerAccessorFactory timelineManagerAccessorFactory, TimelinePosition position,
            TimelineLength length) {
        super(new TimelineInterval(position, length), metadata);
        this.timelineState = timelineState;
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.timelineManagerAccessor = timelineManagerAccessorFactory.createAccessor(timelineState);
        this.timelineManagerRenderService = timelineManagerAccessorFactory.createRenderService(timelineState, timelineManagerAccessor);
        this.mediaMetadata = metadata;
    }

    public SubtimelineAudioClip(SubtimelineAudioClip clip, CloneRequestMetadata cloneRequestMetadata) {
        super(clip, cloneRequestMetadata);
        this.timelineManagerAccessorFactory = clip.timelineManagerAccessorFactory;
        this.timelineState = clip.timelineState.deepClone(cloneRequestMetadata);
        this.timelineManagerAccessor = clip.timelineManagerAccessorFactory.createAccessor(timelineState);
        this.timelineManagerRenderService = clip.timelineManagerAccessorFactory.createRenderService(timelineState, timelineManagerAccessor);
    }

    public SubtimelineAudioClip(TimelineManagerAccessorFactory timelineManagerAccessorFactory, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(readMetadata(savedClip, loadMetadata), savedClip, loadMetadata);
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

    public static AudioMediaMetadata readMetadata(JsonNode savedClip, LoadMetadata loadMetadata) {
        try {
            var reader = loadMetadata.getObjectMapperUsed().readerFor(AudioMediaMetadata.class);
            return reader.readValue(savedClip.get("metadata"));
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
                .withNeedSound(false)
                .withNeedVideo(true)
                .withPreviewWidth(800) // TODO: find why these dummy values are needed
                .withPreviewHeight(600)
                .build();

        AudioVideoFragment audioVideoFragment = timelineManagerRenderService.getFrame(channelFrameRequest);

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

}
