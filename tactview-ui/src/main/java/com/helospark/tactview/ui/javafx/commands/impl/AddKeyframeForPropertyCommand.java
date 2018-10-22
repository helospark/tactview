package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class AddKeyframeForPropertyCommand implements UiCommand {
    private EffectParametersRepository effectParametersRepository;
    private KeyframeAddedRequest request;

    public AddKeyframeForPropertyCommand(EffectParametersRepository effectParametersRepository, KeyframeAddedRequest request) {
        this.effectParametersRepository = effectParametersRepository;
        this.request = request;
    }

    @Override
    public void execute() {
        effectParametersRepository.keyframeAdded(request);
    }

    @Override
    public void revert() {
        effectParametersRepository.removeKeyframe(request); // TODO: should restore last, not delete
    }

    @Override
    public String toString() {
        return "AddKeyframeForPropertyCommand [request=" + request + "]";
    }

}
