package com.helospark.tactview.core.timeline.subtimeline.video;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.helospark.tactview.core.timeline.subtimeline.TimelineManagerAccessorFactory;

public class SubtimelineVisualClip extends VisualTimelineClip {
    private TimelineManagerAccessor timelineManagerAccessor;
    private TimelineManagerRenderService timelineManagerRenderService;
    private TimelineChannelsState timelineState;
    private TimelineManagerAccessorFactory timelineManagerAccessorFactory;

    private Set<ExposedDescriptorDescriptor> enabledDescriptors = new HashSet<>();

    public SubtimelineVisualClip(SubtimelineVisualMetadata metadata, TimelineChannelsState timelineState, TimelineManagerAccessorFactory timelineManagerAccessorFactory,
            Set<ExposedDescriptorDescriptor> descriptorIds,
            TimelinePosition position,
            TimelineLength length) {
        super(metadata, new TimelineInterval(position, length), TimelineClipType.IMAGE);
        this.timelineManagerAccessorFactory = timelineManagerAccessorFactory;
        this.timelineManagerAccessor = timelineManagerAccessorFactory.createAccessor(timelineState);
        this.timelineManagerRenderService = timelineManagerAccessorFactory.createRenderService(timelineState, timelineManagerAccessor);
        this.timelineState = timelineState;
        this.mediaMetadata = metadata;
        this.enabledDescriptors = descriptorIds;
    }

    public SubtimelineVisualClip(SubtimelineVisualClip clip, CloneRequestMetadata cloneRequestMetadata) {
        super(clip, cloneRequestMetadata);
        this.timelineState = clip.timelineState.deepClone(cloneRequestMetadata);
        this.timelineManagerAccessorFactory = clip.timelineManagerAccessorFactory;
        this.timelineManagerAccessor = clip.timelineManagerAccessorFactory.createAccessor(this.timelineState);
        this.timelineManagerRenderService = clip.timelineManagerAccessorFactory.createRenderService(this.timelineState, timelineManagerAccessor);

        if (!cloneRequestMetadata.isDeepCloneId()) {
            enabledDescriptors = clip.enabledDescriptors.stream()
                    .map(a -> a.butWithId(cloneRequestMetadata.getPreviousId(a.getId())))
                    .collect(Collectors.toSet());
        }
    }

    public SubtimelineVisualClip(TimelineManagerAccessorFactory timelineManagerAccessorFactory, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(readMetadata(savedClip, loadMetadata), savedClip, loadMetadata);
        this.timelineState = new TimelineChannelsState();
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

    public TimelineManagerAccessor getTimelineManagerAccessorInternal() {
        return timelineManagerAccessor;
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        for (var channel : this.timelineManagerAccessor.getChannels()) {
            for (var clip : channel.getAllClips()) {
                for (var descriptor : clip.getDescriptors()) {
                    Optional<ExposedDescriptorDescriptor> enabledDescriptorProperty = findDescriptorById(descriptor.getKeyframeableEffect().getId());
                    if (enabledDescriptorProperty.isPresent()) {
                        ValueProviderDescriptor newDescriptor = ValueProviderDescriptor.builderFrom(descriptor)
                                .withName(Optional.ofNullable(enabledDescriptorProperty.get().getName()).orElse(descriptor.getName()))
                                .withGroup(Optional.ofNullable(enabledDescriptorProperty.get().getGroup()).orElse(clip.getClass().getSimpleName() + " " + descriptor.getGroup() + " properties"))
                                .build();
                        result.add(newDescriptor);
                    }
                }
            }
        }

        return result;
    }

    private Optional<ExposedDescriptorDescriptor> findDescriptorById(String id) {
        return enabledDescriptors
                .stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

}
