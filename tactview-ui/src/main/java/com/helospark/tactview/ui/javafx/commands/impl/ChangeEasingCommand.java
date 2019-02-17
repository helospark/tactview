package com.helospark.tactview.ui.javafx.commands.impl;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed.MixedDoubleInterpolator;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ChangeEasingCommand implements UiCommand {
    private String descriptorId;
    private String newEasingId;
    private TimelinePosition position;
    private EffectParametersRepository effectParametersRepository;

    private String currentEasingFunctionId;

    @Generated("SparkTools")
    private ChangeEasingCommand(Builder builder) {
        this.descriptorId = builder.descriptorId;
        this.newEasingId = builder.newEasingId;
        this.position = builder.position;
        this.effectParametersRepository = builder.effectParametersRepository;
    }

    @Override
    public void execute() {
        Object interpolator = effectParametersRepository.getCurrentInterpolator(descriptorId);
        if (interpolator instanceof MixedDoubleInterpolator) {
            MixedDoubleInterpolator mixedInterpolator = (MixedDoubleInterpolator) interpolator;
            currentEasingFunctionId = mixedInterpolator.getEasingFunctionAt(position);
            mixedInterpolator.changeEasingAt(position, newEasingId);
        }
    }

    @Override
    public void revert() {
        Object interpolator = effectParametersRepository.getCurrentInterpolator(descriptorId);
        if (interpolator instanceof MixedDoubleInterpolator) {
            MixedDoubleInterpolator mixedInterpolator = (MixedDoubleInterpolator) interpolator;
            mixedInterpolator.changeEasingAt(position, currentEasingFunctionId);
        }
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String descriptorId;
        private String newEasingId;
        private TimelinePosition position;
        private EffectParametersRepository effectParametersRepository;

        private Builder() {
        }

        public Builder withDescriptorId(String descriptorId) {
            this.descriptorId = descriptorId;
            return this;
        }

        public Builder withNewEasingId(String newEasingId) {
            this.newEasingId = newEasingId;
            return this;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withEffectParametersRepository(EffectParametersRepository effectParametersRepository) {
            this.effectParametersRepository = effectParametersRepository;
            return this;
        }

        public ChangeEasingCommand build() {
            return new ChangeEasingCommand(this);
        }
    }

}
