package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.Optional;

import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ExpressionChangedForPropertyCommand implements UiCommand {
    private EffectParametersRepository effectParametersRepository;
    private KeyframeAddedRequest request;
    private Optional<String> previousValue;

    public ExpressionChangedForPropertyCommand(EffectParametersRepository effectParametersRepository, KeyframeAddedRequest request) {
        this.effectParametersRepository = effectParametersRepository;
        this.request = request;
    }

    @Override
    public void execute() {
        previousValue = request.getPreviousValue().map(a -> (String) a);
        if (!previousValue.isPresent()) {
            previousValue = effectParametersRepository.getExpressionValue(request.getDescriptorId());
        }
        effectParametersRepository.expressionChanged(request);
    }

    @Override
    public void revert() {
        if (previousValue.isPresent()) {
            effectParametersRepository.expressionChanged(request);
        } else {
            effectParametersRepository.expressionRemoved(request.getDescriptorId());
        }
    }

    @Override
    public boolean isRevertable() {
        return request.isRevertable();
    }

    @Override
    public String toString() {
        return "AddExpressionForPropertyCommand [effectParametersRepository=" + effectParametersRepository + ", request=" + request + ", previousValue=" + previousValue + "]";
    }

}
