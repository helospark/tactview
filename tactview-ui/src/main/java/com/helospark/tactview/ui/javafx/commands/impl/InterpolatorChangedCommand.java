package com.helospark.tactview.ui.javafx.commands.impl;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class InterpolatorChangedCommand implements UiCommand {
    private String descriptorId;
    private String newInterpolatorId;
    private EffectParametersRepository effectParametersRepository;

    private Object currentInterpolator;

    @Generated("SparkTools")
    private InterpolatorChangedCommand(Builder builder) {
        this.descriptorId = builder.descriptorId;
        this.newInterpolatorId = builder.newInterpolatorId;
        this.effectParametersRepository = builder.effectParametersRepository;
    }

    @Override
    public void execute() {
        currentInterpolator = effectParametersRepository.getCurrentInterpolator(descriptorId);
        effectParametersRepository.changeInterpolator(descriptorId, newInterpolatorId);
    }

    @Override
    public void revert() {
        effectParametersRepository.changeInterpolatorToInstance(descriptorId, currentInterpolator);
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String descriptorId;
        private String newInterpolatorId;
        private EffectParametersRepository effectParametersRepository;

        private Builder() {
        }

        public Builder withDescriptorId(String descriptorId) {
            this.descriptorId = descriptorId;
            return this;
        }

        public Builder withNewInterpolatorId(String newInterpolatorId) {
            this.newInterpolatorId = newInterpolatorId;
            return this;
        }

        public Builder withEffectParametersRepository(EffectParametersRepository effectParametersRepository) {
            this.effectParametersRepository = effectParametersRepository;
            return this;
        }

        public InterpolatorChangedCommand build() {
            return new InterpolatorChangedCommand(this);
        }
    }
}
