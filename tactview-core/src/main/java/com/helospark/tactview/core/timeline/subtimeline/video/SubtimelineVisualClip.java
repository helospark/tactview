package com.helospark.tactview.core.timeline.subtimeline.video;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
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
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.subtimeline.ExposedDescriptorDescriptor;
import com.helospark.tactview.core.timeline.subtimeline.SubtimelineHelper;
import com.helospark.tactview.core.timeline.subtimeline.TimelineManagerAccessorFactory;

public class SubtimelineVisualClip extends VisualTimelineClip {
    private TimelineManagerAccessor timelineManagerAccessor;
    private TimelineManagerRenderService timelineManagerRenderService;
    private TimelineChannelsState timelineState;
    private TimelineManagerAccessorFactory timelineManagerAccessorFactory;
    private SubtimelineHelper subtimelineHelper;

    private Set<ExposedDescriptorDescriptor> enabledDescriptors = new HashSet<>();

    public SubtimelineVisualClip(SubtimelineVisualMetadata metadata, TimelineChannelsState timelineState, TimelineManagerAccessorFactory timelineManagerAccessorFactory,
            SubtimelineHelper subtimelineHelper,
            Set<ExposedDescriptorDescriptor> descriptorIds,
            TimelinePosition position,
            TimelineLength length) {
        super(metadata, new TimelineInterval(position, length), TimelineClipType.IMAGE);
        this.subtimelineHelper = subtimelineHelper;
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.timelineManagerAccessor = timelineManagerAccessorFactory.createAccessor(timelineState);
        this.timelineManagerRenderService = timelineManagerAccessorFactory.createRenderService(timelineState, timelineManagerAccessor);
        this.timelineState = timelineState;
        this.mediaMetadata = metadata;
        this.enabledDescriptors = descriptorIds;
    }

    public SubtimelineVisualClip(SubtimelineVisualClip clip, CloneRequestMetadata cloneRequestMetadata) {
        super(clip, cloneRequestMetadata);
        this.subtimelineHelper = clip.subtimelineHelper;
        this.timelineState = clip.timelineState.deepClone(cloneRequestMetadata);
        this.timelineManagerAccessorFactory = clip.timelineManagerAccessorFactory;
        this.timelineManagerAccessor = clip.timelineManagerAccessorFactory.createAccessor(this.timelineState);
        this.timelineManagerRenderService = clip.timelineManagerAccessorFactory.createRenderService(this.timelineState, timelineManagerAccessor);

        this.enabledDescriptors = subtimelineHelper.copyExposedDescriptors(cloneRequestMetadata, clip.enabledDescriptors);
    }

    public SubtimelineVisualClip(TimelineManagerAccessorFactory timelineManagerAccessorFactory, SubtimelineHelper subtimelineHelper, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(SubtimelineHelper.readMetadata(savedClip, loadMetadata, SubtimelineVisualMetadata.class), savedClip, loadMetadata);
        this.timelineState = new TimelineChannelsState();
        this.subtimelineHelper = subtimelineHelper;
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.timelineManagerAccessor = timelineManagerAccessorFactory.createAccessor(timelineState);
        this.timelineManagerRenderService = timelineManagerAccessorFactory.createRenderService(timelineState, timelineManagerAccessor);

        try {
            timelineManagerAccessor.loadFrom(savedClip, loadMetadata);
            ObjectReader reader = loadMetadata.getObjectMapperUsed().readerFor(new TypeReference<Set<ExposedDescriptorDescriptor>>() {
            });
            this.enabledDescriptors = reader.readValue(savedClip.get("exposedDescriptors"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent, SaveMetadata saveMetadata) {
        this.timelineManagerAccessor.generateSavedContent(savedContent, saveMetadata);

        savedContent.put("metadata", this.getMediaMetadata());
        savedContent.put("exposedDescriptors", enabledDescriptors);
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

        AudioVideoFragment audioVideoFragment = timelineManagerRenderService.getFrame(channelFrameRequest).getAudioVideoFragment();

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

    public TimelineManagerAccessor getTimelineManagerAccessorInternal() {
        return timelineManagerAccessor;
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        subtimelineHelper.addDescriptorsFromTimeline(result, this.timelineManagerAccessor, enabledDescriptors);

        return result;
    }

}
