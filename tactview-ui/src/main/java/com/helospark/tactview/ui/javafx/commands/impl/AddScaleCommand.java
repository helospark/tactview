package com.helospark.tactview.ui.javafx.commands.impl;

import javax.annotation.Generated;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.EffectFactoryChain;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.CreateEffectRequest;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.core.timeline.effect.scale.ScaleEffect;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class AddScaleCommand implements UiCommand {
    private TimelineManagerAccessor timelineManager;
    private EffectFactory scaleEffectFactory;
    private ProjectRepository projectRepository;

    private String clipId;

    private ScaleEffect addedEffect;

    private EffectFactoryChain effectFactoryChain;

    @Generated("SparkTools")
    private AddScaleCommand(Builder builder) {
        this.timelineManager = builder.timelineManager;
        this.scaleEffectFactory = builder.scaleEffectFactory;
        this.projectRepository = builder.projectRepository;
        this.clipId = builder.clipId;
        this.effectFactoryChain = builder.effectFactoryChain;
    }

    @Override
    public void execute() {
        VisualTimelineClip clip = (VisualTimelineClip) timelineManager.findClipById(clipId).orElseThrow();

        VisualMediaMetadata metadata = clip.getMediaMetadata();
        double scaleX = (double) projectRepository.getWidth() / metadata.getWidth();
        double scaleY = (double) projectRepository.getHeight() / metadata.getHeight();

        CreateEffectRequest createEffectRequest = new CreateEffectRequest(TimelinePosition.ofZero(), scaleEffectFactory.getEffectId(), TimelineClipType.VIDEO, clip.getInterval());

        addedEffect = (ScaleEffect) effectFactoryChain.createEffect(createEffectRequest);
        addedEffect.setScale(scaleX, scaleY);
        addedEffect.setInterval(clip.getInterval().butMoveStartPostionTo(TimelinePosition.ofZero())); // due to relative position

        timelineManager.addEffectForClip(clip, addedEffect);
    }

    @Override
    public void revert() {
        timelineManager.removeEffect(addedEffect.getId());
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelineManagerAccessor timelineManager;
        private EffectFactory scaleEffectFactory;
        private ProjectRepository projectRepository;
        private String clipId;
        private EffectFactoryChain effectFactoryChain;

        private Builder() {
        }

        public Builder withTimelineManager(TimelineManagerAccessor timelineManager) {
            this.timelineManager = timelineManager;
            return this;
        }

        public Builder withScaleEffectFactory(EffectFactory scaleEffectFactory) {
            this.scaleEffectFactory = scaleEffectFactory;
            return this;
        }

        public Builder withProjectRepository(ProjectRepository projectRepository) {
            this.projectRepository = projectRepository;
            return this;
        }

        public Builder withClipId(String clipId) {
            this.clipId = clipId;
            return this;
        }

        public Builder withEffectFactoryChain(EffectFactoryChain effectFactoryChain) {
            this.effectFactoryChain = effectFactoryChain;
            return this;
        }

        public AddScaleCommand build() {
            return new AddScaleCommand(this);
        }
    }
}
