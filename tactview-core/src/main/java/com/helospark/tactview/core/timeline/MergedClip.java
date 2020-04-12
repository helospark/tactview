package com.helospark.tactview.core.timeline;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.framemerge.FrameBufferMerger;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.longprocess.LongProcessRequestor;
import com.helospark.tactview.core.timeline.render.FrameExtender;
import com.helospark.tactview.core.util.messaging.MessagingService;

public class MergedClip extends TimelineClip implements VisualClipAwareTimelineClip, AudioAwareTimelineClip {
    FrameBufferMerger frameBufferMerger;
    AudioBufferMerger audioBufferMerger;
    ProjectRepository projectRepository;
    FrameExtender frameExtender;
    MessagingService messagingService;
    ClipFactoryChain clipFactoryChain;
    EffectFactoryChain effectFactoryChain;
    LinkClipRepository linkClipRepository;
    LongProcessRequestor longProcessRequestor;

    private TimelineChannelsState channelState;

    private TimelineManagerRenderService timelineManagerRenderService;
    private TimelineManagerAccessor accessor;

    public MergedClip(TimelineInterval interval, TimelineClipType type, TimelineChannelsState channelState, FrameBufferMerger frameBufferMerger, AudioBufferMerger audioBufferMerger,
            ProjectRepository projectRepository,
            FrameExtender frameExtender, MessagingService messagingService, ClipFactoryChain clipFactoryChain, EffectFactoryChain effectFactoryChain, LinkClipRepository linkClipRepository,
            LongProcessRequestor longProcessRequestor) {
        super(interval, type);

        this.channelState = channelState;
        this.frameBufferMerger = frameBufferMerger;
        this.audioBufferMerger = audioBufferMerger;
        this.projectRepository = projectRepository;
        this.frameExtender = frameExtender;
        this.messagingService = messagingService;
        this.clipFactoryChain = clipFactoryChain;
        this.effectFactoryChain = effectFactoryChain;
        this.linkClipRepository = linkClipRepository;
        this.longProcessRequestor = longProcessRequestor;
        this.channelState = channelState;

        initializeRenderService(frameBufferMerger, audioBufferMerger, projectRepository, frameExtender, messagingService, clipFactoryChain, effectFactoryChain, linkClipRepository,
                longProcessRequestor, channelState);
    }

    public MergedClip(MergedClip clip, CloneRequestMetadata cloneRequestMetadata) {
        super(clip, cloneRequestMetadata);
        this.channelState = clip.channelState.cloneAll(cloneRequestMetadata);
        this.frameBufferMerger = clip.frameBufferMerger;
        this.audioBufferMerger = clip.audioBufferMerger;
        this.projectRepository = clip.projectRepository;
        this.frameExtender = clip.frameExtender;
        this.messagingService = clip.messagingService;
        this.clipFactoryChain = clip.clipFactoryChain;
        this.effectFactoryChain = clip.effectFactoryChain;
        this.linkClipRepository = clip.linkClipRepository;
        this.longProcessRequestor = clip.longProcessRequestor;

        initializeRenderService(frameBufferMerger, audioBufferMerger, projectRepository, frameExtender, messagingService, clipFactoryChain, effectFactoryChain, linkClipRepository,
                longProcessRequestor, channelState);
    }

    public MergedClip(JsonNode savedClip, LoadMetadata loadMetadata) {
        super(savedClip, loadMetadata);
    }

    // TODO: Use interface segregation on render service, because this is ridiculous
    protected void initializeRenderService(FrameBufferMerger frameBufferMerger, AudioBufferMerger audioBufferMerger, ProjectRepository projectRepository, FrameExtender frameExtender,
            MessagingService messagingService, ClipFactoryChain clipFactoryChain, EffectFactoryChain effectFactoryChain, LinkClipRepository linkClipRepository,
            LongProcessRequestor longProcessRequestor, TimelineChannelsState channelState) {
        accessor = new TimelineManagerAccessor(messagingService, clipFactoryChain, effectFactoryChain, linkClipRepository, channelState, longProcessRequestor,
                projectRepository);
        timelineManagerRenderService = new TimelineManagerRenderService(frameBufferMerger, audioBufferMerger, projectRepository, frameExtender, channelState, accessor);
    }

    @Override
    protected void initializeValueProvider() {
        channelState.channels.stream()
                .map(a -> a.getReadonlyClips())
                .flatMap(a -> a.stream())
                .forEach(a -> a.initializeValueProvider());

        channelState.channels.stream()
                .map(a -> a.getReadonlyClips())
                .flatMap(a -> a.stream())
                .flatMap(a -> a.getEffects().stream())
                .forEach(a -> a.initializeValueProvider());
    }

    @Override
    protected List<ValueProviderDescriptor> getDescriptorsInternal() {
        return null;
    }

    @Override
    public boolean isResizable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent) {
        // TODO Auto-generated method stub

    }

    @Override
    public AudioFrameResult requestAudioFrame(AudioRequest audioRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ReadOnlyClipImage getFrame(GetFrameRequest request) {
        timelineManagerRenderService.getFrame(null);
        return null;
    }

    @Override
    public BlendModeStrategy getBlendModeAt(TimelinePosition position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getAlpha(TimelinePosition position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public BiFunction<Integer, Integer, Integer> getVerticalAlignment(TimelinePosition timelinePosition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BiFunction<Integer, Integer, Integer> getHorizontalAlignment(TimelinePosition timelinePosition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getXPosition(TimelinePosition timelinePosition, double scale) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getYPosition(TimelinePosition timelinePosition, double scale) {
        // TODO Auto-generated method stub
        return 0;
    }

}
