package com.helospark.tactview.core.it.util.ui;

import java.io.File;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class FakeUi {
    private TimelineManager timelineManager;
    private MessagingService messagingService;
    private EffectParametersRepository parametersRepository;

    public FakeUi(TimelineManager timelineManager, EffectParametersRepository parametersRepository) {
        this.timelineManager = timelineManager;
        this.parametersRepository = parametersRepository;
    }

    @PostConstruct
    public void init() {
        for (int i = 0; i < 4; ++i) {
            timelineManager.createChannel(i);
        }
    }

    public TimelineClip dragProceduralClipToFirstChannel(String proceduralClipId, TimelinePosition position) {
        TimelineChannel channel = timelineManager.getChannels().get(0);

        AddClipRequest request = AddClipRequest.builder()
                .withChannelId(channel.getId())
                .withFilePath(null)
                .withPosition(position)
                .withProceduralClipId(proceduralClipId)
                .build();

        return timelineManager.addClip(request);
    }

    public void enableKeyframesFor(String clipId, String descriptorName) {
        enableKeyframes(clipId, descriptorName, true);
    }

    public void disableKeyframesFor(String clipId, String descriptorName) {
        enableKeyframes(clipId, descriptorName, false);
    }

    private void enableKeyframes(String clipId, String descriptorName, boolean status) {
        ValueProviderDescriptor descriptor = parametersRepository.findDescriptorForLabelAndClipId(clipId, descriptorName).get();
        KeyframeableEffect provider = descriptor.getKeyframeableEffect();

        parametersRepository.setUsingKeyframes(provider.getId(), status);
    }

    public void setKeyframeForColorDescriptor(String clipId, String descriptorName, TimelinePosition position, Color color) {
        ValueProviderDescriptor descriptor = parametersRepository.findDescriptorForLabelAndClipId(clipId, descriptorName).get();
        ColorProvider colorProvider = (ColorProvider) descriptor.getKeyframeableEffect();

        setKeyframeForChild(position, () -> String.valueOf(color.red), 0, colorProvider);
        setKeyframeForChild(position, () -> String.valueOf(color.green), 1, colorProvider);
        setKeyframeForChild(position, () -> String.valueOf(color.blue), 2, colorProvider);
    }

    private void setKeyframeForChild(TimelinePosition position, Supplier<String> supplier, int index, ColorProvider colorProvider) {
        KeyframeAddedRequest keyframeAddedRequest = KeyframeAddedRequest.builder()
                .withDescriptorId(colorProvider.getChildren().get(index).getId())
                .withGlobalTimelinePosition(position)
                .withValue(supplier.get())
                .build();
        parametersRepository.keyframeAdded(keyframeAddedRequest);
    }

    public AudioVideoFragment requestPreviewVideoFrame(TimelinePosition position) {
        return requestPreviewVideoFrameWithScale(position, 1.0);
    }

    public AudioVideoFragment requestPreviewVideoFrameWithScale(TimelinePosition position, double scale) {
        TimelineManagerFramesRequest frameRequest = TimelineManagerFramesRequest.builder()
                .withFrameBufferSize(1)
                .withNeedSound(false)
                .withPosition(position)
                .withPreviewWidth(600)
                .withPreviewHeight(400)
                .withScale(scale)
                .build();

        return timelineManager.getSingleFrame(frameRequest);
    }

    public TimelineClip dragFileToTimeline(File testFile, TimelinePosition position) {
        TimelineChannel channel = timelineManager.getChannels().get(0);

        AddClipRequest request = AddClipRequest.builder()
                .withChannelId(channel.getId())
                .withFilePath(testFile.getAbsolutePath())
                .withPosition(position)
                .build();

        return timelineManager.addClip(request);
    }

}