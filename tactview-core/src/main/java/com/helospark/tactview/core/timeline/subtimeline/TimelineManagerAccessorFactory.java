package com.helospark.tactview.core.timeline.subtimeline;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudioBufferMerger;
import com.helospark.tactview.core.timeline.ClipFactoryChain;
import com.helospark.tactview.core.timeline.EffectFactoryChain;
import com.helospark.tactview.core.timeline.LinkClipRepository;
import com.helospark.tactview.core.timeline.TimelineChannelsState;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.framemerge.FrameBufferMerger;
import com.helospark.tactview.core.timeline.longprocess.LongProcessRequestor;
import com.helospark.tactview.core.timeline.render.FrameExtender;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class TimelineManagerAccessorFactory {
    private final ClipFactoryChain clipFactoryChain;
    private final EffectFactoryChain effectFactoryChain;
    private final LinkClipRepository linkClipRepository;
    private final LongProcessRequestor longProcessRequestor;
    private final ProjectRepository projectRepository;
    private final FrameBufferMerger frameBufferMerger;
    private final AudioBufferMerger audioBufferMerger;
    private final FrameExtender frameExtender;

    public TimelineManagerAccessorFactory(ClipFactoryChain clipFactoryChain, EffectFactoryChain effectFactoryChain, LinkClipRepository linkClipRepository,
            LongProcessRequestor longProcessRequestor, ProjectRepository projectRepository, FrameBufferMerger frameBufferMerger, AudioBufferMerger audioBufferMerger, FrameExtender frameExtender,
            MessagingService messagingService) {
        this.clipFactoryChain = clipFactoryChain;
        this.effectFactoryChain = effectFactoryChain;
        this.linkClipRepository = linkClipRepository;
        this.longProcessRequestor = longProcessRequestor;
        this.projectRepository = projectRepository;
        this.frameBufferMerger = frameBufferMerger;
        this.audioBufferMerger = audioBufferMerger;
        this.frameExtender = frameExtender;
    }

    public TimelineManagerAccessor createAccessor(TimelineChannelsState timelineState, MessagingService messagingService) {
        TimelineManagerAccessor result = new TimelineManagerAccessor(messagingService, clipFactoryChain, effectFactoryChain, linkClipRepository, timelineState, longProcessRequestor,
                projectRepository);
        result.postConstruct();
        return result;
    }

    public TimelineManagerRenderService createRenderService(TimelineChannelsState timelineState, TimelineManagerAccessor accessor, MessagingService messagingService) {
        return new TimelineManagerRenderService(frameBufferMerger, audioBufferMerger, projectRepository, frameExtender, timelineState, accessor);
    }

}
