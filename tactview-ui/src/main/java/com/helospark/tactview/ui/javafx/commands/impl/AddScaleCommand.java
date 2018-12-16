package com.helospark.tactview.ui.javafx.commands.impl;

import javax.annotation.Generated;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.CreateEffectRequest;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.core.timeline.effect.scale.ScaleEffect;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class AddScaleCommand implements UiCommand {
    private TimelineManager timelineManager;
    private EffectFactory scaleEffectFactory;
    private ProjectRepository projectRepository;

    private String clipId;

    private ScaleEffect addedEffect;

    @Generated("SparkTools")
    private AddScaleCommand(Builder builder) {
        this.timelineManager = builder.timelineManager;
        this.scaleEffectFactory = builder.scaleEffectFactory;
        this.projectRepository = builder.projectRepository;
        this.clipId = builder.clipId;
    }

    @Override
    public void execute() {
        VisualTimelineClip clip = (VisualTimelineClip) timelineManager.findClipById(clipId).orElseThrow();

        CreateEffectRequest request = new CreateEffectRequest(TimelinePosition.ofZero(), scaleEffectFactory.getEffectId(), TimelineClipType.VIDEO);

        VisualMediaMetadata metadata = clip.getMediaMetadata();
        double scaleX = (double) projectRepository.getWidth() / metadata.getWidth();
        double scaleY = (double) projectRepository.getHeight() / metadata.getHeight();

        addedEffect = (ScaleEffect) scaleEffectFactory.createEffect(request);
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
        private TimelineManager timelineManager;
        private EffectFactory scaleEffectFactory;
        private ProjectRepository projectRepository;
        private String clipId;

        private Builder() {
        }

        public Builder withTimelineManager(TimelineManager timelineManager) {
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

        public AddScaleCommand build() {
            return new AddScaleCommand(this);
        }
    }
}
