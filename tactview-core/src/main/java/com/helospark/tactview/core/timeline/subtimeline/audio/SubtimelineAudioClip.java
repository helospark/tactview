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
import com.helospark.tactview.core.timeline.subtimeline.AfterClipAddedListener;
import com.helospark.tactview.core.timeline.subtimeline.ClipContainingClip;
import com.helospark.tactview.core.timeline.subtimeline.DelayedMessagingService;
import com.helospark.tactview.core.timeline.subtimeline.TimelineManagerAccessorFactory;
import com.helospark.tactview.core.util.messaging.MessagingService;

public class SubtimelineAudioClip extends AudibleTimelineClip implements ClipContainingClip, AfterClipAddedListener {
    private TimelineManagerAccessor timelineManagerAccessor;
    private TimelineManagerRenderService timelineManagerRenderService;
    private TimelineChannelsState timelineState;
    private TimelineManagerAccessorFactory timelineManagerAccessorFactory;
    private DelayedMessagingService delayedMessagingService;

    public SubtimelineAudioClip(AudioMediaMetadata metadata, TimelineChannelsState timelineState, TimelineManagerAccessorFactory timelineManagerAccessorFactory, MessagingService messagingService,
            TimelinePosition position,
            TimelineLength length) {
        super(new TimelineInterval(position, length), metadata);
        this.delayedMessagingService = new DelayedMessagingService(messagingService);
        this.timelineState = timelineState;
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.timelineManagerAccessor = timelineManagerAccessorFactory.createAccessor(timelineState, delayedMessagingService);
        this.timelineManagerRenderService = timelineManagerAccessorFactory.createRenderService(timelineState, timelineManagerAccessor, delayedMessagingService);
        this.mediaMetadata = metadata;
    }

    public SubtimelineAudioClip(SubtimelineAudioClip clip, CloneRequestMetadata cloneRequestMetadata) {
        super(clip, cloneRequestMetadata);
        this.delayedMessagingService = clip.delayedMessagingService;
        this.timelineManagerAccessorFactory = clip.timelineManagerAccessorFactory;
        this.timelineState = clip.timelineState.deepClone(cloneRequestMetadata);
        this.timelineManagerAccessor = clip.timelineManagerAccessorFactory.createAccessor(timelineState, delayedMessagingService);
        this.timelineManagerRenderService = clip.timelineManagerAccessorFactory.createRenderService(timelineState, timelineManagerAccessor, delayedMessagingService);
    }

    public SubtimelineAudioClip(TimelineManagerAccessorFactory timelineManagerAccessorFactory, MessagingService messagingService, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(readMetadata(savedClip, loadMetadata), savedClip, loadMetadata);
        this.delayedMessagingService = new DelayedMessagingService(messagingService);
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.timelineState = new TimelineChannelsState();
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.timelineManagerAccessor = timelineManagerAccessorFactory.createAccessor(timelineState, delayedMessagingService);
        this.timelineManagerRenderService = timelineManagerAccessorFactory.createRenderService(timelineState, timelineManagerAccessor, delayedMessagingService);

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

    @Override
    public Optional<TimelineClip> findClipById(String id) {
        return this.timelineManagerAccessor.findClipById(id);
    }

    @Override
    public void afterClipAdded() {
        delayedMessagingService.stopDelay();
        for (var channel : this.timelineState.getChannels()) {
            for (var clip : channel.getAllClips()) {
                timelineManagerAccessor.sendClipAndEffectMessages(channel, clip);
            }
        }
    }

}
