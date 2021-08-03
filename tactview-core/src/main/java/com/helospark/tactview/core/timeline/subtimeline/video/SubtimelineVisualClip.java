package com.helospark.tactview.core.timeline.subtimeline.video;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.RequestFrameParameter;
import com.helospark.tactview.core.timeline.TimelineChannelsState;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.subtimeline.AfterClipAddedListener;
import com.helospark.tactview.core.timeline.subtimeline.ClipContainingClip;
import com.helospark.tactview.core.timeline.subtimeline.DelayedMessagingService;
import com.helospark.tactview.core.timeline.subtimeline.TimelineManagerAccessorFactory;
import com.helospark.tactview.core.util.messaging.MessagingService;

public class SubtimelineVisualClip extends VisualTimelineClip implements ClipContainingClip, AfterClipAddedListener {
    private TimelineManagerAccessor timelineManagerAccessor;
    private TimelineManagerRenderService timelineManagerRenderService;
    private TimelineChannelsState timelineState;
    private TimelineManagerAccessorFactory timelineManagerAccessorFactory;
    private DelayedMessagingService delayedMessagingService;

    public SubtimelineVisualClip(SubtimelineVisualMetadata metadata, TimelineChannelsState timelineState, TimelineManagerAccessorFactory timelineManagerAccessorFactory,
            MessagingService messagingService, TimelinePosition position,
            TimelineLength length) {
        super(metadata, new TimelineInterval(position, length), TimelineClipType.IMAGE);
        delayedMessagingService = new DelayedMessagingService(messagingService);
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.timelineManagerAccessor = timelineManagerAccessorFactory.createAccessor(timelineState, delayedMessagingService);
        this.timelineManagerRenderService = timelineManagerAccessorFactory.createRenderService(timelineState, timelineManagerAccessor, delayedMessagingService);
        this.timelineState = timelineState;
        this.mediaMetadata = metadata;
    }

    public SubtimelineVisualClip(SubtimelineVisualClip clip, CloneRequestMetadata cloneRequestMetadata) {
        super(clip, cloneRequestMetadata);
        this.timelineState = clip.timelineState.deepClone(cloneRequestMetadata);
        this.delayedMessagingService = clip.delayedMessagingService;
        this.timelineManagerAccessorFactory = clip.timelineManagerAccessorFactory;
        this.timelineManagerAccessor = clip.timelineManagerAccessorFactory.createAccessor(this.timelineState, delayedMessagingService);
        this.timelineManagerRenderService = clip.timelineManagerAccessorFactory.createRenderService(this.timelineState, timelineManagerAccessor, delayedMessagingService);
    }

    public SubtimelineVisualClip(TimelineManagerAccessorFactory timelineManagerAccessorFactory, MessagingService messagingService, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(readMetadata(savedClip, loadMetadata), savedClip, loadMetadata);
        this.timelineState = new TimelineChannelsState();
        delayedMessagingService = new DelayedMessagingService(messagingService);
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.timelineManagerAccessor = timelineManagerAccessorFactory.createAccessor(timelineState, delayedMessagingService);
        this.timelineManagerRenderService = timelineManagerAccessorFactory.createRenderService(timelineState, timelineManagerAccessor, delayedMessagingService);

        try {
            timelineManagerAccessor.loadFrom(savedClip, loadMetadata);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SubtimelineVisualMetadata readMetadata(JsonNode savedClip, LoadMetadata loadMetadata) {
        try {
            var reader = loadMetadata.getObjectMapperUsed().readerFor(SubtimelineVisualMetadata.class);
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
    public ReadOnlyClipImage requestFrame(RequestFrameParameter request) {
        TimelineManagerFramesRequest channelFrameRequest = TimelineManagerFramesRequest.builder()
                .withPosition(request.getPosition())
                .withScale(request.getScale())
                .withPreviewWidth(request.getWidth())
                .withPreviewHeight(request.getHeight())
                .withNeedSound(false)
                .withNeedVideo(true)
                .build();

        AudioVideoFragment audioVideoFragment = timelineManagerRenderService.getFrame(channelFrameRequest);

        audioVideoFragment.getAudioResult().free();

        return audioVideoFragment.getVideoResult();
    }

    @Override
    public VisualMediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new SubtimelineVisualClip(this, cloneRequestMetadata);
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
