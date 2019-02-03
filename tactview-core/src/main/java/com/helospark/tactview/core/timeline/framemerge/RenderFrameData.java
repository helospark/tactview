package com.helospark.tactview.core.timeline.framemerge;

import java.util.List;
import java.util.Optional;

import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class RenderFrameData {
    public double globalAlpha;
    public BlendModeStrategy blendModeStrategy;
    public ReadOnlyClipImage clipFrameResult;
    public String channelId;
    public String id;
    public Optional<AbstractVideoTransitionEffect> videoTransition;

    public RenderFrameData(String id, double globalAlpha, BlendModeStrategy blendModeStrategy, ReadOnlyClipImage clipFrameResult, List<AbstractVideoTransitionEffect> list, String channelId) {
        this.id = id;
        this.globalAlpha = globalAlpha;
        this.blendModeStrategy = blendModeStrategy;
        this.clipFrameResult = clipFrameResult;
        videoTransition = list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.get(0)); // should multiple transition be handled?
        this.channelId = channelId;
    }

}
