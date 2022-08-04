package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.Optional;

import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ExpressionRemovedForPropertyCommand implements UiCommand {
    private EffectParametersRepository effectParametersRepository;
    private String descriptorId;
    private Optional<String> previousValue;

    public ExpressionRemovedForPropertyCommand(EffectParametersRepository effectParametersRepository, String descriptorId) {
        this.effectParametersRepository = effectParametersRepository;
        this.descriptorId = descriptorId;
    }

    @Override
    public void execute() {
        previousValue = effectParametersRepository.getExpressionValue(descriptorId);
        effectParametersRepository.expressionRemoved(descriptorId);
    }

    @Override
    public void revert() {
        if (previousValue.isPresent()) {
            var keyframeAddedRequest = KeyframeAddedRequest.builder()
                    .withDescriptorId(descriptorId)
                    .withValue(previousValue)
                    .build();
            effectParametersRepository.expressionChanged(keyframeAddedRequest);
        } else {
            effectParametersRepository.expressionRemoved(descriptorId);
        }
    }

    @Override
    public String toString() {
        return "ExpressionRemovedForPropertyCommand [effectParametersRepository=" + effectParametersRepository + ", descriptorId=" + descriptorId + ", previousValue=" + previousValue + "]";
    }

}
