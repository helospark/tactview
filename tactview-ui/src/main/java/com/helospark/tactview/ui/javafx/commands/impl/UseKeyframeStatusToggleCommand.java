package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class UseKeyframeStatusToggleCommand implements UiCommand {
    private EffectParametersRepository effectParametersRepository;
    private String keyframeableEffectId;

    private Boolean previousValue;

    public UseKeyframeStatusToggleCommand(EffectParametersRepository effectParametersRepository, String keyframeableEffectId) {
        this.effectParametersRepository = effectParametersRepository;
        this.keyframeableEffectId = keyframeableEffectId;
    }

    @Override
    public void execute() {
        previousValue = effectParametersRepository.isUsingKeyframes(keyframeableEffectId);
        effectParametersRepository.setUsingKeyframes(keyframeableEffectId, !previousValue);
    }

    @Override
    public void revert() {
        effectParametersRepository.setUsingKeyframes(keyframeableEffectId, previousValue);
    }

    @Override
    public String toString() {
        return "UseKeyframeStatusToggleCommand [effectParametersRepository=" + effectParametersRepository + ", keyframeableEffectId=" + keyframeableEffectId + ", previousValue=" + previousValue + "]";
    }

}
