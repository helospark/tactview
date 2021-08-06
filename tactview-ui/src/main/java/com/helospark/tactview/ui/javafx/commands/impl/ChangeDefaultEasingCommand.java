package com.helospark.tactview.ui.javafx.commands.impl;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed.EaseFunction;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed.MixedDoubleInterpolator;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ChangeDefaultEasingCommand implements UiCommand {
    private String descriptorId;
    private String newEasingId;
    private EffectParametersRepository effectParametersRepository;

    private EaseFunction currentEasingFunctionId;

    @Generated("SparkTools")
    private ChangeDefaultEasingCommand(Builder builder) {
        this.descriptorId = builder.descriptorId;
        this.newEasingId = builder.newEasingId;
        this.effectParametersRepository = builder.effectParametersRepository;
    }

    @Override
    public void execute() {
        Object interpolator = effectParametersRepository.getCurrentInterpolator(descriptorId);
        if (interpolator instanceof MixedDoubleInterpolator) {
            MixedDoubleInterpolator mixedInterpolator = (MixedDoubleInterpolator) interpolator;
            currentEasingFunctionId = mixedInterpolator.getDefaultEaseFunction();
            mixedInterpolator.changeDefaultEaseFunction(EaseFunction.fromId(newEasingId));
        }
    }

    @Override
    public void revert() {
        Object interpolator = effectParametersRepository.getCurrentInterpolator(descriptorId);
        if (interpolator instanceof MixedDoubleInterpolator) {
            MixedDoubleInterpolator mixedInterpolator = (MixedDoubleInterpolator) interpolator;
            mixedInterpolator.changeDefaultEaseFunction(currentEasingFunctionId);
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

        public Builder withEffectParametersRepository(EffectParametersRepository effectParametersRepository) {
            this.effectParametersRepository = effectParametersRepository;
            return this;
        }

        public ChangeDefaultEasingCommand build() {
            return new ChangeDefaultEasingCommand(this);
        }
    }

}
